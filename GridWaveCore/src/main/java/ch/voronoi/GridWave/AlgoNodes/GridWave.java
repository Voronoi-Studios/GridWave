package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.*;
import ch.voronoi.GridWave.FeatureNodes.DebugFeatureAsset;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.FeatureNodes.Helper.EarlyExitReason;
import ch.voronoi.GridWave.FeatureNodes.MultithreadingFeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridWave {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final Vector3ic[] dirs = { Vector3iUtil.NORTH, Vector3iUtil.EAST, Vector3iUtil.SOUTH, Vector3iUtil.WEST };
    
    public static class WFCResult {
        public SeedBox seedBox;
        public Bounds3i bounds;
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
    public static @NonNull List<GridTile> solve(List<Vector3dc> gridPositions, List<TileSet> poiTileEntries, List<TileSet> baseTileEntries, List<TileSet> fancyTileEntries, TileSetAsset.Argument argument) {
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
    public static @NonNull Map<Vector3ic, WaveCell> getBaseWave(@NonNull List<Vector3dc> gridPositions, @NonNull List<TileSet> poiTileEntries, @NonNull List<TileSet> baseTileEntries, TileSetAsset.Argument argument){
        List<FeatureAsset> featureAssets = argument.algoAsset.getFeatureAssets();
        Map<Vector3ic, WaveCell> baseWave = new HashMap<>();
        gridPositions.forEach(pos -> baseWave.put(toCellPos(pos, argument.algoAsset.getGrid()), new WaveCell(toCellPos(pos, argument.algoAsset.getGrid()), Vector3dUtil.toVector3i((Vector3d)pos), new LinkedHashSet<>(baseTileEntries.stream().flatMap(TileSet::getTileEntries).toList()))));

        featureAssets.forEach(feature -> feature.BaseWaveProcessor(baseWave, argument));

        Map<Vector3ic, LinkedHashSet<POIInfo>> poiGroupMap = new HashMap<>();

        //Replace with restrained tiles
        for(TileSet tileSet : poiTileEntries.stream().toList()){
            for(TileEntry tileEntry : tileSet.getTileEntries().toList()){
                Vector3ic absolutePos = tileEntry.restrained();
                if (absolutePos == null) continue;
                for(TileEntry subtileEntry : tileEntry.getSubTiles()){
                    if(subtileEntry.getMainRuleSet().equals(RuleCombo.H_EMPTY)) continue;
                    if(subtileEntry.getMainRuleSet().equals(RuleCombo.H_NULL)) continue;
                    if(subtileEntry.getMainRuleSet().equals(RuleCombo.H_ALL_N)) continue;
                    //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced

                    Vector3ic posKey = new Vector3i(absolutePos).add(subtileEntry.mainKey());
                    if(baseWave.containsKey(posKey)) { //How do we deal with those for debug grid?
                        WaveCell waveCell = baseWave.get(posKey);
                        waveCell.setChosen(subtileEntry, GridTileType.POI);
                        waveCell.connectedPOIs = poiGroupMap.computeIfAbsent(subtileEntry.getPoiKey(), k -> new LinkedHashSet<>(Set.of(new POIInfo(k))));
                        propagate(waveCell, baseWave, null,argument);
                    }
                }
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
    public static @NonNull Map<Vector3ic, WaveCell> performWFC(Map<Vector3ic, WaveCell> baseWave, TileSetAsset.Argument argument, WFCResult wfcResult) {
        List<FeatureAsset> featureAssets = argument.algoAsset.getFeatureAssets();
        MultithreadingFeatureAsset.Context multithreadContext = argument.algoAsset.getFirstFeatureOf(MultithreadingFeatureAsset.class).map(asset -> asset.get(argument)).orElse(null);
        int participantNumber = multithreadContext != null ? multithreadContext.participantNumber().incrementAndGet() : 0;
        SeedBox childSeedBox = multithreadContext != null ? argument.seedBox.child(participantNumber + "s") : argument.seedBox;
        Map<Vector3ic, WaveCell> wave = new LinkedHashMap<>();
        int backtracksCount = -1;
        int attempt = 0;
        boolean sucess = true;
        SeedBox attemptSeedBox = null;

        AttemptBehavior attemptBehavior = new AttemptBehavior(10, 5000,baseWave.size());
        featureAssets.forEach(feature -> feature.BeforeWFC(attemptBehavior, argument));

        //Default Greedy Lowest Entropy
        AtomicReference<CellSelector> cellSelectorRef = new AtomicReference<>(new CellSelector() {
            @Override public CellSelectorResult select(Map<Vector3ic, WaveCell> wave, Stack<List<WaveCellChange>> undoQue, AttemptBehavior attemptBehavior, int backtracksCount, Random random) {
                Optional<WaveCell> lowestEntropyCell = wave.values().stream().filter(waveCell -> !waveCell.isCollapsed()).min(Comparator.comparingInt(WaveCell::getEntropy));
                if (lowestEntropyCell.isPresent() && lowestEntropyCell.get().getEntropy() == 0) {
                    if (backtracksCount > attemptBehavior.maxBacktracks) return new CellSelectorResult(null, EarlyExitReason.MAX_BACKTRACKS_HIT);
                    else return Backtrack(undoQue, wave);
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
                wave.clear(); baseWave.forEach((k, v) -> wave.put(k, new WaveCell(v)));
                Stack<List<WaveCellChange>> undoQue = new Stack<>();
                sucess = false;

                backtracksCount = 0;
                int collapsedCount = 0;

                while (collapsedCount < attemptBehavior.maxCollapsedCount) {
                    if(multithreadContext != null && multithreadContext.winner().get() != null) break; //Give up LOOSER!

                    CellSelectorResult result = cellSelector.select(wave, undoQue, attemptBehavior, backtracksCount, randomSupplier);
                    WaveCell selectedCell = result.selectedCell();
                    if (result.earlyExitReason() == EarlyExitReason.BACKTRACKED) { backtracksCount += 1; collapsedCount -= 1; continue; }
                    if (result.earlyExitReason() == EarlyExitReason.MAX_BACKTRACKS_HIT){ break; } //Failed
                    if (selectedCell == null) { sucess = true; break; } //finished => can this even happen?

                    //Collapse
                    var waveCellChange = new WaveCellChange(selectedCell.getGridPosition(), new WaveCell(selectedCell));
                    selectedCell.collapse(randomSupplier, wave, argument);
                    waveCellChange.cell().possible.remove(selectedCell.getChosen().tileEntry()); //We remove the chosen one so if we backtrack it is not tried again
                    undoQue.push(new LinkedList<>(List.of(waveCellChange)));
                    collapsedCount++;

                    //Propagate to neighbors
                    propagate(selectedCell, wave, undoQue, argument);
                }
                if (sucess || collapsedCount >= attemptBehavior.maxCollapsedCount) {
                    sucess = featureAssets.stream().allMatch(feature -> feature.FinalCheck(wave, participantNumber, argument));
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
     * @param undoQue A que to keep track of changes for backtracking purposes; can be null if backtracking is not needed
     */
    public static void propagate(WaveCell source, Map<Vector3ic, WaveCell> wave, Stack<List<WaveCellChange>> undoQue, TileSetAsset.Argument argument) {
        IntStream.range(0, 4).forEach(dir -> {
            Vector3ic neighborPos = getNeighborPos(source.getGridPosition(), dir, argument);
            WaveCell neighbor = wave.get(neighborPos);
            if (neighbor != null){
                if(undoQue != null && !undoQue.isEmpty()) undoQue.peek().add(new WaveCellChange(neighborPos, neighbor));
                if(!neighbor.isCollapsed()){
                    neighbor.possible.removeIf(tileEntry -> !Match.dir(dir, tileEntry.getMainRuleSet(), source.getChosen().tileEntry().getMainRuleSet()));
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
    public static @NonNull Map<Vector3ic, WaveCell> placeFancyTiles(Map<Vector3ic, WaveCell> wave, @NonNull List<TileSet> fancyTileEntries, TileSetAsset.Argument argument){
        Map<Vector3ic, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        if(argument.algoAsset.getFirstFeatureOf(DebugFeatureAsset.class).orElse(new DebugFeatureAsset()).getSkipFancyTiles()) return fancyWave;
        Random randomSupplier = new Random(argument.seedBox.child("fancy").createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries.stream().flatMap(TileSet::getTileEntries).toList()){
                boolean fullFancyMatch = true;
                for(var subRuleSet : fancyTileEntry.ruleSets().entrySet()){
                    Vector3ic key = new Vector3i(waveCellEntry.getKey()).add(new Vector3i(subRuleSet.getKey()));
                    if(!fancyWave.containsKey(key)) { fullFancyMatch = false; break;}
                    var chosen= fancyWave.get(key).getChosen();
                    if (chosen == null || chosen.type() != GridTileType.BASIC) { fullFancyMatch = false; break; }
                    if (!Match.fancyMatch(subRuleSet.getValue(),chosen.tileEntry().getMainRuleSet())) {
                        fullFancyMatch = false; break;
                    }
                }
                if(!fullFancyMatch) continue;
                if (randomSupplier.nextDouble(1) > fancyTileEntry.weight()) continue; //To-DO: Implement WeightedMap

                for(var subTiles : fancyTileEntry.getSubTiles()){
                    Vector3ic key = new Vector3i(waveCellEntry.getKey()).add(new Vector3i(subTiles.mainKey()));
                    if(subTiles.getMainRuleSet().equals(RuleCombo.H_EMPTY)) continue;
                    if(subTiles.getMainRuleSet().equals(RuleCombo.H_NULL)) continue;
                    if(subTiles.getMainRuleSet().equals(RuleCombo.H_ALL_N)) continue;
                    //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced
                    fancyWave.get(key).setChosen(subTiles, GridTileType.FANCY);
                }
                break;
            }
        }
        return fancyWave;
    }

    /*===========================================================
    *                         UTILITY
    * =========================================================== */
    public static List<Vector3dc> getPositions(PositionProvider provider, Bounds3i bounds, int maxPositionsCount) {
        List<Vector3dc> positions = new ArrayList<>();

        Pipe.One<Vector3d> collectingPipe = (position, control) -> {
            if (positions.size() < maxPositionsCount) {
                positions.add(new Vector3d(position));
            }
        };

        PositionProvider.Context context = new PositionProvider.Context(bounds.toBounds3d(), collectingPipe, null);
        provider.generate(context);
        return positions;
    }

    public static Vector3ic toCellPos(Vector3dc pos, Vector3ic grid) {
        return new Vector3i(
                (int) Math.floor(pos.x() / (double) grid.x()) * grid.x(),
                (int) Math.floor(pos.y() / (double) grid.y()) * grid.y(),
                (int) Math.floor(pos.z() / (double) grid.z()) * grid.z()
        );
    }

    public static @NonNull Vector3ic getNeighborPos(Vector3ic source, int dir, TileSetAsset.Argument argument) {
        return new Vector3i(source).add(new Vector3i(dirs[dir]).mul(argument.algoAsset.getGrid()));
    }
}
