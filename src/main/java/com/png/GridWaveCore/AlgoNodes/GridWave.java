package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.png.GridWaveCore.AlgoNodes.Helper.*;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;
import com.png.GridWaveCore.FeatureNodes.MultithreadingAsset;
import com.png.GridWaveCore.FeatureNodes.OverlapTileAsset;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.TileNodes.TileSet;

import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3d;

import com.png.GridWaveCore.TileNodes.TileSetAsset;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class GridWave {
    public static final Vector3ic[] dirs = { Vector3iUtil.NORTH, Vector3iUtil.EAST, Vector3iUtil.SOUTH, Vector3iUtil.WEST };

    private static final ConcurrentHashMap<String, AtomicReference<Map<Vector3ic, WaveCell>>> winnerGridTilesMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AtomicReference<Winner>> winnerMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AtomicInteger> participantTracker = new ConcurrentHashMap<>();
    public record Winner(int workerId, SeedBox seedBox, int backtracks, int attempts) {}

    /*===========================================================
    *                     GET BASE WAVE
    * =========================================================== */
    /** Creates the base grid of WaveCells and adds any fixed tiles (Points of Interest) to it. 
     * It also makes sure any tiles with no neighbor (border tiles) get propagated according to the border rules.
    * @param poiTileEntries Fixed tiles (Points of Interest)
    * @param baseTileEntries Base TileSet entries that define the possible tiles for the wave initialization
    * @param gridPositions Grid positions for wave initialization, typically generated from a PositionProvider
    * @param grid Size of the grid step, used to get neighbor positions
    * @return BaseWave represented as a map of grid positions to WaveCells*/
    public static @NonNull Map<Vector3ic, WaveCell> getBaseWave(@NonNull List<TileSet.TileEntry> poiTileEntries, @NonNull List<TileSet.TileEntry> baseTileEntries, @NonNull List<Vector3dc> gridPositions, int grid, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset){
        Map<Vector3ic, WaveCell> baseWave = new HashMap<>();
        gridPositions.forEach(pos -> baseWave.put(Vector3dUtil.toVector3i( new Vector3d(pos)), new WaveCell(Vector3dUtil.toVector3i(new Vector3d(pos)), new LinkedHashSet<>(baseTileEntries))));

        featureAssets.forEach(feature -> feature.BaseWaveProcessor(gridPositions, grid, baseWave, featureAssets, algoAsset));

        //Replace fixed tiles
        for(TileSet.TileEntry tileEntry : poiTileEntries){
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.EMPTY)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.NULL)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.ALL_N)) continue;
            //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced

            if(baseWave.containsKey(tileEntry.identifierKey())) { //Maybe if debug add them?
                WaveCell waveCell = baseWave.get(tileEntry.identifierKey());
                waveCell.setChosen(tileEntry, GridTileType.POI);
                propagate(waveCell, baseWave, null, grid, featureAssets, algoAsset);
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
    * @param grid Size of the grid step, used to get neighbor positions
    * @param seedBox SeedBox for randomization
    * @param workerId Identifier for the worker thread (used in multithreading)
    * @return Map of grid positions to WaveCells representing the collapsed wave, or null if no solution found in multithreading mode*/
    public static @NonNull Map<Vector3ic, WaveCell> performWFC(Map<Vector3ic, WaveCell> baseWave, int grid, SeedBox seedBox, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset, int workerId) {
        boolean multithreading = featureAssets.stream().anyMatch(feature -> feature instanceof MultithreadingAsset a && !a.skip());
        SeedBox childSeedBox = multithreading ? seedBox.child(workerId + "s") : seedBox;
        SeedBox attemptSeedBox = null;
        AtomicReference<Map<Vector3ic, WaveCell>> winnerGridTiles = winnerGridTilesMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>());
        AtomicReference<Winner> winner = winnerMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>(null));
        AtomicInteger participants = participantTracker.computeIfAbsent(seedBox.toString(), k -> new AtomicInteger());
        participants.getAndAdd(1);

        if(participants.get() == 1){ //I'm the first so lets restart the race.
            winnerGridTiles.set(new LinkedHashMap<>());
            winner.set(null);
        }

        Map<Vector3ic, WaveCell> wave = new LinkedHashMap<>();
        int backtracksCount = -1;
        int attempt = -1;

        AttemptBehavior attemptBehavior = new AttemptBehavior();

        featureAssets.forEach(feature -> feature.BeforeWFC(attemptBehavior, featureAssets, algoAsset));

        boolean replaced = featureAssets.stream().anyMatch(feature -> feature.WFCReplacer(baseWave, featureAssets, algoAsset));
        if(!replaced) {

            while (attempt < attemptBehavior.maxAttempts) { attempt++;
                attemptSeedBox = childSeedBox.child(attempt + "a");
                Random randomSupplier = new Random(attemptSeedBox.createSupplier().get());
                wave = new LinkedHashMap<>(baseWave);

                Deque<WaveCellChange> stack = new ArrayDeque<>();
                boolean failed = false;

                backtracksCount = 0;
                int collapsedCount = 0;
                while (collapsedCount < baseWave.size()) {
                    if(multithreading && winner.get() != null) break; //Give up LOOSER!

                    //Find cell with the lowest entropy
                    WaveCell lowestEntropy = null;
                    int minEntropy = Integer.MAX_VALUE;

                    for (WaveCell e : wave.values()) {
                        if (e.isCollapsed()) continue;
                        int entropy = e.getEntropy();
                        if (entropy == 0) { //Dead End
                            if(backtracksCount > attemptBehavior.maxBacktracks) { lowestEntropy = null; failed = true; break; }
                            WaveCellChange change = null;
                            for (int i = 0; i < 5 && !stack.isEmpty(); i++) {
                                change = stack.pop();
                                if (change.cell() != null) wave.put(change.pos(), change.cell());
                            }
                            backtracksCount++;
                            collapsedCount--;
                            failed = true; break;
                        }
                        if (entropy < minEntropy) {
                            minEntropy = entropy;
                            lowestEntropy = e;
                        }
                    }

                    if (lowestEntropy == null) break; //No collapsible cell found, either finished or failed
                    if (failed) { failed = false; continue; }

                    //Collapse
                    var waveCellChange = new WaveCellChange(lowestEntropy.getPosition(), lowestEntropy);
                    lowestEntropy.collapse(randomSupplier);
                    waveCellChange.cell().possible.remove(lowestEntropy.getChosen().tileEntry());
                    stack.push(waveCellChange);
                    collapsedCount++;

                    //Propagate to neighbors
                    propagate(lowestEntropy, wave, stack, grid, featureAssets, algoAsset);
                }
                if (!failed && featureAssets.stream().anyMatch(feature -> feature.FinalCheck(baseWave, featureAssets, algoAsset))) break; //finished
            }
        }

        if (multithreading) {
            if (winner.get() == null && winnerGridTiles.compareAndSet(null, new LinkedHashMap<>(wave))) {
                winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt)); }
        } else if(workerId == 1) winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt));

        participants.getAndAdd(-1);

        return multithreading ? new LinkedHashMap<>(winnerGridTiles.get()) : wave;
    }


    /*===========================================================
    *                     PROPAGATION & MATCHING
    * =========================================================== */
    /**Removes all non-matching tiles from the neighbors list of possible base tiles
     * @param source The cell from which to propagate constraints
     * @param wave The current state of the wave
     * @param stack A stack to keep track of changes for backtracking purposes; can be null if backtracking is not needed
     * @param grid The size of the grid step, used to calculate neighbor positions
     */
    public static void propagate(WaveCell source, Map<Vector3ic, WaveCell> wave, Deque<WaveCellChange> stack, int grid, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        
        
        IntStream.range(0, 4).forEach(rot -> {
            Vector3ic neighborPos = new Vector3i(source.getPosition()).add(new Vector3i(dirs[rot]).mul(grid)); //Maybe make it so we have a rounded grid position and an actuall offset position
            WaveCell neighbor = wave.get(neighborPos);
            if(stack != null) stack.push(new WaveCellChange(neighborPos, neighbor));
            if (neighbor != null){
                if(!neighbor.isCollapsed()){
                    neighbor.possible.removeIf(tileEntry -> !Match.dir(rot, tileEntry.getMainRuleSet(), source.getChosen().tileEntry().getMainRuleSet()));
                }
                featureAssets.forEach(feature -> feature.AfterNeighbourPropagation(source, rot, neighbor, featureAssets, algoAsset));
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
     * @param seedBox SeedBox for deterministic randomization
     * @return A new wave map with fancy tiles placed according to the defined rules and random chance*/
    public static @NonNull Map<Vector3ic, WaveCell> placeFancyTiles(Map<Vector3ic, WaveCell> wave, @NonNull List<TileSet.TileEntry> fancyTileEntries, SeedBox seedBox){
        Map<Vector3ic, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        Random randomSupplier = new Random(seedBox.createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries){
                boolean fullMatch = true;
                for(var subRuleSet : fancyTileEntry.ruleSets().entrySet()){
                    Vector3ic key = new Vector3i(waveCellEntry.getKey()).add(subRuleSet.getKey());
                    if(!fancyWave.containsKey(key)) { fullMatch = false; break;}
                    var chosen= fancyWave.get(key).getChosen();
                    if (chosen == null || chosen.type() != GridTileType.BASIC) { fullMatch = false; break; }
                    if (!Match.full(subRuleSet.getValue(),chosen.tileEntry().getMainRuleSet())) { fullMatch = false; break;}
                }
                if(!fullMatch) continue;
                if (randomSupplier.nextDouble(1) > fancyTileEntry.weight()) continue; //To-DO: Implement WeightedMap

                for(var subTiles : fancyTileEntry.getSubTiles()){
                    Vector3ic key = new Vector3i(waveCellEntry.getKey()).add(subTiles.identifierKey());
                    if(Match.is(subTiles.getMainRuleSet(), RuleSet.Combo.EMPTY)) continue;
                    if(Match.is(subTiles.getMainRuleSet(), RuleSet.Combo.NULL)) continue;
                    if(Match.is(subTiles.getMainRuleSet(), RuleSet.Combo.ALL_N)) continue;
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
     * @param grid The size of the grid step, used to calculate prop offsets
     * @param gridTiles Our list of GridTiles representing the final collapsed wave
     * @return A map of world positions (Vector3d) to Props*/
    public static @NonNull Map<Vector3dc, Prop> loadPrefabProps(TileSetAsset.Argument argument, int grid, List<GridTile> gridTiles, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        Map<Vector3dc, Prop> gridProps = new LinkedHashMap<>();
        boolean swap = featureAssets.stream().anyMatch(feature -> feature instanceof OverlapTileAsset);
        for (var gridTile : gridTiles) {
            if (gridTile == null) continue;
            TileSet.TileEntry tileEntry = gridTile.tileEntry();
            Vector3ic[] anchorOffsets = getAnchorOffsets(grid,swap || tileEntry.tileFeatures().stream().anyMatch(feature -> feature instanceof OverlapTileAsset));
            Prop prop = Optional.ofNullable(tileEntry.propFunction()).map(f -> f.apply(argument)).orElse(EmptyProp.INSTANCE);
            if(prop.equals(EmptyProp.INSTANCE)) continue;
            Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(tileEntry.rotation(), Rotation.None, Rotation.None), argument.materialCache);
            Vector3ic offset = new Vector3i(gridTile.positionOffset()).add(tileEntry.getOffset()).add(anchorOffsets[tileEntry.rot()]);
            gridProps.put(Vector3iUtil.toVector3d(offset), rotatedProp);
        }
        return gridProps;
    }

    public static @NonNull Vector3ic[] getAnchorOffsets(int grid, boolean swap) {
        int evenOffset = (grid % 2 == 0) ? 1 : 0;
        if (swap) evenOffset = 1 - evenOffset;

        return new Vector3i[] {
                new Vector3i(0, 0, 0),
                new Vector3i(0, 0, evenOffset),
                new Vector3i(evenOffset, 0, evenOffset),
                new Vector3i(evenOffset, 0, 0)
        };
    }

    /*===========================================================
    *                         UTILITY
    * =========================================================== */
    public static int getGrid(List<Vector3dc> gridPositions) {
        return (int) Math.round(gridPositions
                .stream()
                .flatMap(p1 -> gridPositions.stream().map(p1::distance))
                .filter(d -> d > 0)
                .min(Double::compare)
                .orElse(0.0)
        );
    }

    public static List<Vector3dc> getPositions(PositionProvider provider, int maxPositionsCount) {
        List<Vector3dc> positions = new ArrayList<>();

        Pipe.One<Vector3d> collectingPipe = (position, control) -> {
            if (positions.size() < maxPositionsCount) {
                positions.add(new Vector3d(position));
            }
        };
        PositionProvider.Context context = new PositionProvider.Context(
                new Bounds3d(
                        new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
                        new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)),
                collectingPipe,
                null
        );
        provider.generate(context);
        return positions;
    }
}
