package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.png.GridWaveCore.AlgoNodes.WFC.*;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.TileFeatures.TileFeatureAsset;
import com.png.GridWaveCore.TileNodes.SingleTileSet;
import com.png.GridWaveCore.TileNodes.TileSet;

import com.png.GridWaveCore.TileNodes.TileSetAsset;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridWave {
    static final Vector3i[] dirs = { Vector3i.NORTH, Vector3i.EAST, Vector3i.SOUTH, Vector3i.WEST };

    private static final ConcurrentHashMap<String, AtomicReference<Map<Vector3i, WaveCell>>> winnerGridTilesMap = new ConcurrentHashMap<>();
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
    * @param borderRuleSet Border RuleSet
    * @param debug If true, generates a simplified wave for testing purposes, without border propagation.
    * @return BaseWave represented as a map of grid positions to WaveCells*/
    public static @NonNull Map<Vector3i, WaveCell> getBaseWave(@NonNull List<TileSet.TileEntry> poiTileEntries, @NonNull List<TileSet.TileEntry> baseTileEntries, @NonNull List<Vector3d> gridPositions, int grid, RuleSet.Combo borderRuleSet, RuleSet.Combo pathRuleSet,  boolean debug){
        Map<Vector3i, WaveCell> baseWave = new HashMap<>();
        gridPositions.forEach(pos -> baseWave.put(pos.toVector3i(), new WaveCell(pos.toVector3i(), new LinkedHashSet<>(baseTileEntries))));

        if(!debug){ //Edge detected
            Set<Vector3i> borderPositions = new LinkedHashSet<>();
            for (Vector3d pos3d : gridPositions) {
                for (int d = 0; d < 4; d++) {
                    Vector3i neighbor = new Vector3i(pos3d.toVector3i()).add(dirs[d].clone().scale(grid));
                    if (!baseWave.containsKey(neighbor)) borderPositions.add(neighbor); //If its an outer tile
                }
            }

            TileSet.TileEntry borderTile = new SingleTileSet(new WeightedMap<>(), borderRuleSet,1,false, new ArrayList<>(0)).getTileEntries().getFirst();
            for(Vector3i borderPos : borderPositions){
                WaveCell waveCell = new WaveCell(borderPos.clone(),borderTile, GridTileType.BASIC);
                propagate(waveCell, baseWave, null, grid, pathRuleSet);
            }
        }


        //Replace fixed tiles
        for(TileSet.TileEntry tileEntry : poiTileEntries){
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.EMPTY)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.NULL)) continue;
            if(Match.is(tileEntry.getMainRuleSet(), RuleSet.Combo.ALL_N)) continue;
            //RuleSet.Combo.ALL_X has to go through, so for example a 3x3's middle tile also gets replaced

            if(baseWave.containsKey(tileEntry.identifierKey())) { //Maybe if debug add them?
                WaveCell waveCell = baseWave.get(tileEntry.identifierKey());
                waveCell.setChosen(tileEntry, GridTileType.POI);
                propagate(waveCell, baseWave, null, grid, pathRuleSet);
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
    * @param maxAttempts Maximum number of attempts to find a solution, returning the nonfinal wave if exceeded
    * @param maxBacktracks Maximum number of backtracks allowed per attempt
    * @param seedBox SeedBox for randomization
    * @param multithreading If true, multiple threads will race to find a solution
    * @param debug If true, returns a debug wave, {@link GridWave#getDebugWave}
    * @param workerId Identifier for the worker thread (used in multithreading)
    * @return Map of grid positions to WaveCells representing the collapsed wave, or null if no solution found in multithreading mode*/
    public static @NonNull Map<Vector3i, WaveCell> performWFC(Map<Vector3i, WaveCell> baseWave, int grid, int maxAttempts, int maxBacktracks, SeedBox seedBox, RuleSet.Combo pathRuleSet, int POIsCount, boolean multithreading, boolean debug, int workerId) {
        SeedBox childSeedBox = multithreading ? seedBox.child(workerId + "s") : seedBox;
        SeedBox attemptSeedBox = null;
        AtomicReference<Map<Vector3i, WaveCell>> winnerGridTiles = winnerGridTilesMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>());
        AtomicReference<Winner> winner = winnerMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>(null));
        AtomicInteger participants = participantTracker.computeIfAbsent(seedBox.toString(), k -> new AtomicInteger());
        participants.getAndAdd(1);

        if(participants.get() == 1){ //I'm the first so lets restart the race.
            winnerGridTiles.set(new LinkedHashMap<>());
            winner.set(null);
        }

        Map<Vector3i, WaveCell> wave = new LinkedHashMap<>();
        int backtracksCount = -1;
        int attempt = -1;

        if(debug) wave = getDebugWave(baseWave);
        else while (attempt < maxAttempts) { attempt++;
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
                        if(backtracksCount > maxBacktracks) { lowestEntropy = null; failed = true; break; }
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
                propagate(lowestEntropy, wave, stack, grid, pathRuleSet);
            }
            if (!failed && arePOIsConnected(baseWave, POIsCount)) break; //finished
        }

        if (multithreading) {
            if (winner.get() == null && winnerGridTiles.compareAndSet(null, new LinkedHashMap<>(wave))) {
                winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt)); }
        } else if(workerId == 1) winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt));

        participants.getAndAdd(-1);

        return multithreading ? new LinkedHashMap<>(winnerGridTiles.get()) : wave;
    }
    /** Generates a simplified wave for testing purposes, chronologically collapsing all tiles
     * bottom left to top right and loops through all tile variants (rotations)*/
    private static @NonNull Map<Vector3i, WaveCell> getDebugWave(Map<Vector3i, WaveCell> baseWave) {
        Map<Vector3i, WaveCell> wave = sortByXThenZ(baseWave);

        int counter = 0;
        for(Map.Entry<Vector3i, WaveCell> entry : wave.entrySet()){
            if (entry.getValue().isCollapsed()) continue;
            List<TileSet.TileEntry> possibles = new ArrayList<>(entry.getValue().possible);
            entry.getValue().setChosen(possibles.get(counter % possibles.size()), GridTileType.BASIC);
            counter++;
        }
        return wave;
    }
    private static @NonNull LinkedHashMap<Vector3i, WaveCell> sortByXThenZ(Map<Vector3i, WaveCell> baseWave) {
        return baseWave.entrySet().stream()
            .sorted(Comparator
                    .comparingInt((Map.Entry<Vector3i, WaveCell> e) -> e.getKey().x)
                    .thenComparingInt(e -> e.getKey().z))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new
            ));
    }


    private static boolean arePOIsConnected(Map<Vector3i, WaveCell> baseWave, int POIsCount) {
        return baseWave.values().stream().anyMatch(x -> x.connectedPOIs.size() >= POIsCount);
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
    private static void propagate(WaveCell source, Map<Vector3i, WaveCell> wave, Deque<WaveCellChange> stack, int grid, RuleSet.Combo pathRuleSet) {
        IntStream.range(0, 4).forEach(rot -> {
            Vector3i neighborPos = new Vector3i(source.getPosition()).add(dirs[rot].clone().scale(grid)); //Maybe make it so we have a rounded grid position and an actuall offset position
            WaveCell neighbor = wave.get(neighborPos);
            if(stack != null) stack.push(new WaveCellChange(neighborPos, neighbor));
            if (neighbor != null){
                if(!neighbor.isCollapsed()){
                    neighbor.possible.removeIf(tileEntry -> !Match.dir(rot, tileEntry.getMainRuleSet(), source.getChosen().tileEntry().getMainRuleSet()));
                } else if(Match.dir(rot, neighbor.getChosen().tileEntry().getMainRuleSet(), pathRuleSet)) {
                    source.connectedPOIs.addAll(neighbor.connectedPOIs);
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
     * @param seedBox SeedBox for deterministic randomization
     * @return A new wave map with fancy tiles placed according to the defined rules and random chance*/
    public static @NonNull Map<Vector3i, WaveCell> placeFancyTiles(Map<Vector3i, WaveCell> wave, @NonNull List<TileSet.TileEntry> fancyTileEntries, SeedBox seedBox){
        Map<Vector3i, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        Random randomSupplier = new Random(seedBox.createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries){
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
    public static @NonNull Map<Vector3d, Prop> loadPrefabProps(TileSetAsset.Argument argument, int grid, List<GridTile> gridTiles) {
        Map<Vector3d, Prop> gridProps = new LinkedHashMap<>();
        Vector3i[] anchorOffsets = getAnchorOffsets(grid);
        for (var gridTile : gridTiles) {
            if (gridTile == null) continue;
            Prop prop = gridTile.tileEntry().propFunction().apply(argument);
            if(prop.equals(EmptyProp.INSTANCE)) continue;
            Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(gridTile.tileEntry().rotation(), Rotation.None, Rotation.None), argument.materialCache);
            Vector3i offset = gridTile.positionOffset().clone().add(gridTile.tileEntry().getOffset().add(anchorOffsets[gridTile.tileEntry().rot()].clone()));
            gridProps.put(offset.toVector3d(), rotatedProp);
        }
        return gridProps;
    }

    public static @NonNull Vector3i[] getAnchorOffsets(int grid) {
        int evenOffset = (grid % 2 == 0) ? 1 : 0;
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
    public static int getGrid(List<Vector3d> gridPositions) {
        return (int) Math.round(gridPositions
                .stream()
                .flatMap(p1 -> gridPositions.stream().map(p1::distanceTo))
                .filter(d -> d > 0)
                .min(Double::compare)
                .orElse(0.0)
        );
    }

    public static List<Vector3d> getPositions(PositionProvider provider, int maxPositionsCount) {
        List<Vector3d> positions = new ArrayList<>();

        Pipe.One<Vector3d> collectingPipe = (position, control) -> {
            if (positions.size() < maxPositionsCount) {
                positions.add(position.clone());
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
