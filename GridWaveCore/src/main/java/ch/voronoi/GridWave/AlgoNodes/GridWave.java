package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.*;
import ch.voronoi.GridWave.FeatureNodes.MultithreadingFeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.FeatureNodes.OverlapTileFeatureAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSet;

import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridWave {
    public static final Vector3i[] dirs = { Vector3i.NORTH, Vector3i.EAST, Vector3i.SOUTH, Vector3i.WEST };
    
    public static class WFCResult {
        public SeedBox seedBox;
        public int backtracks;
        public int attempts;
        public boolean success;
        @Override
        public String toString() {
            return "WFCResult:\nseedBox=" + seedBox + "\nbacktracks=" + backtracks + "\nattempts=" + attempts + "\nsuccess=" + success;
        }
    }
    public record Winner(int particepant, WFCResult wfcResult) {}


    /*===========================================================
     *                      MAIN SOLVER
     * =========================================================== */
    public static @NonNull List<GridTile> solve(List<Vector3d> gridPositions, List<TileSet> poiTileEntries, List<TileSet> baseTileEntries, List<TileSet> fancyTileEntries, TileSetAsset.Argument argument) {
        WFCResult wfcResult = new WFCResult();
        var baseWave = getBaseWave(gridPositions, poiTileEntries, baseTileEntries, argument);
        var wfcWave = performWFC(baseWave, argument, wfcResult);
        var fancyWave = placeFancyTiles(wfcWave, fancyTileEntries, argument);
        List<GridTile> gridTiles = new ArrayList<>(fancyWave.values().stream().map(WaveCell::getChosen).toList());

        DebugUtils.sendDebugLog(gridTiles, argument, wfcResult);

        return gridTiles;
    }

    /*===========================================================
    *                     GET BASE WAVE
    * =========================================================== */
    /** Creates the base grid of WaveCells and adds any fixed tiles (Points of Interest) to it. 
     * It also makes sure any tiles with no neighbor (border tiles) get propagated according to the border rules.
    * @param gridPositions Grid positions for wave initialization, typically generated from a PositionProvider
    * @param poiTileEntries Fixed tiles (Points of Interest)
    * @param baseTileEntries Base TileSet entries that define the possible tiles for the wave initialization
    * @return BaseWave represented as a map of grid positions to WaveCells*/
    public static @NonNull Map<Vector3i, WaveCell> getBaseWave(@NonNull List<Vector3d> gridPositions, @NonNull List<TileSet> poiTileEntries, @NonNull List<TileSet> baseTileEntries, TileSetAsset.Argument argument){
        List<FeatureAsset> featureAssets = argument.algoAsset.getFeatureAssets();
        Map<Vector3i, WaveCell> baseWave = new HashMap<>();
        gridPositions.forEach(pos -> baseWave.put(pos.toVector3i(), new WaveCell(pos.toVector3i(), new LinkedHashSet<>(baseTileEntries.stream().flatMap(TileSet::getAllTileEntries).toList()))));

        featureAssets.forEach(feature -> feature.BaseWaveProcessor(gridPositions, baseWave, argument));

        Map<Vector3i, LinkedHashSet<POIInfo>> poiGroupMap = new HashMap<>();

        //Replace fixed tiles
        for(TileSet.TileEntry tileEntry : poiTileEntries.stream().flatMap(TileSet::getAllTileEntries).toList()){
            if(Match.is(tileEntry.getMainRuleSet(), RuleCombo.H_EMPTY)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleCombo.H_NULL)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleCombo.H_ALL_N)) continue;
            //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced

            if(baseWave.containsKey(tileEntry.identifierKey())) { //Maybe if debug add them?
                WaveCell waveCell = baseWave.get(tileEntry.identifierKey());
                waveCell.setChosen(tileEntry, GridTileType.POI);
                Vector3i pos = waveCell.getPosition().clone().add(tileEntry.getOffset().clone());
                waveCell.connectedPOIs = poiGroupMap.computeIfAbsent(pos, k -> new LinkedHashSet<>(Set.of(new POIInfo(k))));
                propagate(waveCell, baseWave, null,argument);
            }
        }

        return baseWave;
    }



    /*===========================================================
    *                     PERFORM WFC
    * =========================================================== */
    /** Executes the Wave Function Collapse algorithm on the provided BaseWave.
    * Handles cell collapsing, backtracking, and multithreading if used.
    * @param baseWave Initial wave of collapsible tiles
    * @return Map of grid positions to WaveCells representing the collapsed wave, or null if no solution found in multithreading mode*/
    public static @NonNull Map<Vector3i, WaveCell> performWFC(Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument, WFCResult wfcResult) {
        List<FeatureAsset> featureAssets = argument.algoAsset.getFeatureAssets();
        MultithreadingFeatureAsset.Context multithreadContext = argument.getFirstFeatureOf(MultithreadingFeatureAsset.class).get(argument);
        int participantNumber = multithreadContext != null ? multithreadContext.participantNumber().incrementAndGet() : 0;
        SeedBox childSeedBox = multithreadContext != null ? argument.seedBox.child(participantNumber + "s") : argument.seedBox;
        Map<Vector3i, WaveCell> wave = new LinkedHashMap<>();
        int backtracksCount = -1;
        int attempt = 0;
        boolean sucess = true;
        SeedBox attemptSeedBox = null;

        AttemptBehavior attemptBehavior = new AttemptBehavior(10, 5000,baseWave.size());
        featureAssets.forEach(feature -> feature.BeforeWFC(attemptBehavior, argument));

        //Default Greedy Lowest Entropy
        AtomicReference<CellSelector> cellSelectorRef = new AtomicReference<>(new CellSelector() {
            @Override
            public CellSelectorResult select(Map<Vector3i, WaveCell> wave, Deque<WaveCellChange> stack, AttemptBehavior attemptBehavior, int backtracksCount, Random random) {
                Optional<WaveCell> lowestEntropyCell = wave.values().stream().filter(waveCell -> !waveCell.isCollapsed()).min(Comparator.comparingInt(WaveCell::getEntropy));
                if (lowestEntropyCell.isPresent() && lowestEntropyCell.get().getEntropy() == 0) {
                    if (backtracksCount > attemptBehavior.maxBacktracks) return new CellSelectorResult(null, EarlyExitReason.MAX_BACKTRACKS_HIT);
                    else return Backtrack(stack, wave);
                }
                return new CellSelectorResult(lowestEntropyCell.orElse(null), null);
            }
        });
        featureAssets.forEach(feature -> feature.ReplaceCellSelector(cellSelectorRef, argument));

        CellSelector cellSelector = cellSelectorRef.get();

        boolean replaced = featureAssets.stream().anyMatch(feature -> feature.WFCReplacer(baseWave, argument));
        if(!replaced) {
            while (attempt < attemptBehavior.maxAttempts) { attempt++;
                attemptSeedBox = childSeedBox.child(attempt + "a");
                int seed = attemptSeedBox.createSupplier().get();
                Random randomSupplier = new Random(seed);
                wave = baseWave.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,e -> new WaveCell(e.getValue()), (a, b) -> a, LinkedHashMap::new));

                Deque<WaveCellChange> stack = new ArrayDeque<>();
                sucess = false;

                backtracksCount = 0;
                int collapsedCount = 0;

                while (collapsedCount < attemptBehavior.maxCollapsedCount) {
                    if(multithreadContext != null && multithreadContext.winner().get() != null) break; //Give up LOOSER!

                    CellSelectorResult result = cellSelector.select(wave, stack, attemptBehavior, backtracksCount, randomSupplier);
                    WaveCell selectedCell = result.selectedCell();
                    if (result.earlyExitReason() == EarlyExitReason.BACKTRACKED) { backtracksCount += 1; collapsedCount -= 1; continue; }
                    if (result.earlyExitReason() == EarlyExitReason.MAX_BACKTRACKS_HIT){ break; } //Failed
                    if (selectedCell == null) { sucess = true; break; } //finished

                    //Collapse
                    var waveCellChange = new WaveCellChange(selectedCell.getPosition(), new WaveCell(selectedCell));
                    selectedCell.collapse(randomSupplier, wave, argument);
                    waveCellChange.cell().possible.remove(selectedCell.getChosen().tileEntry()); //We remove the choosen one so if we backtrack its not tried again
                    stack.push(waveCellChange);
                    collapsedCount++;

                    //Propagate to neighbors
                    propagate(selectedCell, wave, stack, argument);
                }
                if(sucess) {
                    Map<Vector3i, WaveCell> finalWave = wave;
                    sucess = featureAssets.stream().allMatch(feature -> feature.FinalCheck(finalWave, participantNumber, argument));
                }
                if(sucess) break; //finished
            }
        }

        if(multithreadContext != null) multithreadContext.participantNumber().decrementAndGet();

        wfcResult.attempts = attempt;
        wfcResult.backtracks = backtracksCount;
        wfcResult.seedBox = attemptSeedBox;
        wfcResult.success = sucess;
        return wave;
    }

    /*===========================================================
    *                     PROPAGATION & MATCHING
    * =========================================================== */
    /**Removes all non-matching tiles from the neighbors list of possible base tiles
     * @param source The cell from which to propagate constraints
     * @param wave The current state of the wave
     * @param stack A stack to keep track of changes for backtracking purposes; can be null if backtracking is not needed
     */
    public static void propagate(WaveCell source, Map<Vector3i, WaveCell> wave, Deque<WaveCellChange> stack, TileSetAsset.Argument argument) {
        IntStream.range(0, 4).forEach(rot -> {
            Vector3i neighborPos = new Vector3i(source.getPosition()).add(dirs[rot].clone().scale(argument.algoAsset.getGrid())); //Maybe make it so we have a rounded grid position and an actuall offset position
            WaveCell neighbor = wave.get(neighborPos);
            if(stack != null) stack.push(new WaveCellChange(neighborPos, neighbor));
            if (neighbor != null){
                if(!neighbor.isCollapsed()){
                    neighbor.possible.removeIf(tileEntry -> !Match.dir(rot, tileEntry.getMainRuleSet(), source.getChosen().tileEntry().getMainRuleSet()));
                }
            }
        });
    }


    /*===========================================================
    *                     FANCY TILE PLACEMENT
    * =========================================================== */
    /**Pattern matches the collapsed wave cells RuleSets against the fancy's RuleSets. If they match and the random chance based on the fancy tile's weight succeeds, 
     * the fancy tile replaces the current tile in the wave. This allows to easily add more visual variety or to achieve post-processing effects like connecting platforms with bridges
     * @param wave The current state of the wave
     * @param fancyTileEntries Our list of fancy tiles
     * @return A new wave map with fancy tiles placed according to the defined rules and random chance*/
    public static @NonNull Map<Vector3i, WaveCell> placeFancyTiles(Map<Vector3i, WaveCell> wave, @NonNull List<TileSet> fancyTileEntries, TileSetAsset.Argument argument){
        Map<Vector3i, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        Random randomSupplier = new Random(argument.seedBox.child("fancy").createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries.stream().flatMap(TileSet::getAllTileEntries).toList()){
                boolean fullMatch = true;
                for(var subRuleSet : fancyTileEntry.ruleSets().entrySet()){
                    Vector3i key = waveCellEntry.getKey().clone().add(subRuleSet.getKey().clone());
                    if(!fancyWave.containsKey(key)) { fullMatch = false; break;}
                    var chosen= fancyWave.get(key).getChosen();
                    if (chosen == null || chosen.type() != GridTileType.BASIC) { fullMatch = false; break; }
                    if (!Match.full(subRuleSet.getValue(),chosen.tileEntry().getMainRuleSet())) { fullMatch = false; break;}
                }
                if(!fullMatch) continue;
                if (randomSupplier.nextDouble(1) > fancyTileEntry.weight()) continue; //To-DO: Implement WeightedMap

                for(var subTiles : fancyTileEntry.getSubTiles()){
                    Vector3i key = waveCellEntry.getKey().clone().add(subTiles.identifierKey().clone());
                    if(Match.is(subTiles.getMainRuleSet(), RuleCombo.H_EMPTY)) continue;
                    if(Match.is(subTiles.getMainRuleSet(), RuleCombo.H_NULL)) continue;
                    if(Match.is(subTiles.getMainRuleSet(), RuleCombo.H_ALL_N)) continue;
                    //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced
                    fancyWave.get(key).setChosen(subTiles, GridTileType.FANCY);
                }
                break;
            }
        }
        return fancyWave;
    }


    /*===========================================================
    *                     PROP PLACEMENT
    * =========================================================== */
    /**Converts the final collapsed wave into a map of world positions and Props. 
     * It takes into account the tile's position, rotation, and any defined offsets.
     * @param argument The PropDistributionAsset.Argument containing necessary context for prop creation, such as material cache and seed
     * @param gridTiles Our list of GridTiles representing the final collapsed wave
     * @return A map of world positions (Vector3d) to Props*/
    public static @NonNull Map<Vector3d, Prop> loadPrefabProps(List<GridTile> gridTiles, TileSetAsset.Argument argument) {
        Map<Vector3d, Prop> gridProps = new LinkedHashMap<>();
        boolean swap = argument.algoAsset.getFeatureAssets().stream().anyMatch(feature -> feature instanceof OverlapTileFeatureAsset);
        for (var gridTile : gridTiles) {
            if (gridTile == null) continue;

            TileSet.TileEntry tileEntry = new TileSet.TileEntry(gridTile.tileEntry());
            Vector3i[] anchorOffsets = getAnchorOffsets(argument.algoAsset.getGrid(), swap || tileEntry.tileFeatures().stream().anyMatch(feature -> feature instanceof OverlapTileFeatureAsset));
            TileSetAsset.Argument subArgument = new TileSetAsset.Argument(argument);
            Prop prop = Optional.ofNullable(tileEntry.propFunction()).map(f -> f.apply(subArgument)).orElse(EmptyProp.INSTANCE);
            if(prop.equals(EmptyProp.INSTANCE)) continue;
            Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(tileEntry.rotation(), Rotation.None, Rotation.None), subArgument.materialCache);
            Vector3i offset = gridTile.positionOffset().clone().add(tileEntry.getOffset().clone().add(anchorOffsets[tileEntry.rot()].clone()));
            gridProps.put(offset.toVector3d(), rotatedProp);
        }
        return gridProps;
    }

    public static @NonNull Vector3i[] getAnchorOffsets(Vector3i grid, boolean swap) {
        int evenOffsetX = (grid.x % 2 == 0) ? 1 : 0;
        int evenOffsetZ = (grid.z % 2 == 0) ? 1 : 0;
        if (swap){
            evenOffsetX = 1 - evenOffsetX;
            evenOffsetZ = 1 - evenOffsetZ;
        }

        return new Vector3i[] { //To-Do: Check if this  is actually offsetting correctly for nonuniform grids
                new Vector3i(0, 0, 0),
                new Vector3i(0, 0, evenOffsetZ),
                new Vector3i(evenOffsetX, 0, evenOffsetZ),
                new Vector3i(evenOffsetX, 0, 0)
        };
    }

    /*===========================================================
    *                         UTILITY
    * =========================================================== */
    public static List<Vector3d> getPositions(PositionProvider provider, Bounds3d bounds, int maxPositionsCount) {
        List<Vector3d> positions = new ArrayList<>();

        Pipe.One<Vector3d> collectingPipe = (position, control) -> {
            if (positions.size() < maxPositionsCount) {
                positions.add(position.clone());
            }
        };

        PositionProvider.Context context = new PositionProvider.Context(
                bounds,
                collectingPipe,
                null
        );
        provider.generate(context);
        return positions;
    }
}
