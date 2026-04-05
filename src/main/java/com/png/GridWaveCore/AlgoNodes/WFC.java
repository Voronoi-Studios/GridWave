package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.png.GridWaveCore.TileNodes.TileSet;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WFC {
    static final Vector3i[] dirs = { Vector3i.NORTH, Vector3i.EAST, Vector3i.SOUTH, Vector3i.WEST };
    static final int[] opposite = {2, 3, 0, 1};
    public enum GridTileType { BASIC, POI, FANCY }

    private static final ConcurrentHashMap<String, AtomicReference<Map<Vector3i, WaveCell>>> winnerGridTilesMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicReference<Winner>> winnerMap = new ConcurrentHashMap<>();

    public record Winner(int workerId, SeedBox seedBox, int backtracks, int attempts) {}

    public static @NonNull Map<Vector3i, WaveCell> getBaseWave(
            @NonNull List<TileSet.TileEntry> poiTileEntries,
            @NonNull List<TileSet.TileEntry> baseTileEntries,
            @NonNull List<Vector3d> gridPositions,
            int grid, String[] borderRuleSet, boolean debug)
    {
        Map<Vector3i, WaveCell> baseWave = new HashMap<>();
        for (Vector3d pos : gridPositions) {
            baseWave.put(pos.toVector3i(), new WaveCell(pos.toVector3i(), new LinkedHashSet<>(baseTileEntries)));
        }

        if(!debug){
            //Edge detected
            for (Vector3d pos3d : gridPositions) {
                Vector3i pos = pos3d.toVector3i();
                LinkedHashSet<TileSet.TileEntry> validTileEntries = baseWave.get(pos).possible;

                for (int d = 0; d < 4; d++) {
                    Vector3i neighbor = new Vector3i(pos).add(dirs[d].clone().scale(grid));
                    if (!baseWave.containsKey(neighbor)) { //If its an outer tile
                        final int dir = d;
                        final String required = borderRuleSet[opposite[d]];
                        validTileEntries.removeIf(t -> !match(t.getMainRuleSet()[dir], required));
                    }
                }
            }
        }


        //Replace fixed tiles
        for(TileSet.TileEntry tileEntry : poiTileEntries){
            if(Arrays.equals(tileEntry.getMainRuleSet(), new String[4])) continue;
            if(Arrays.equals(tileEntry.getMainRuleSet(), new String[] {"", "", "", ""})) continue;
            if(baseWave.containsKey(tileEntry.identifierKey())) { //Maybe if debug add them?
                WaveCell waveCell = baseWave.get(tileEntry.identifierKey());
                waveCell.setChosen(tileEntry, GridTileType.POI);
                propagate(waveCell, baseWave, null, grid);
            }
        }

        return baseWave;
    }

    public static Map<Vector3i, WaveCell> performWFC(Map<Vector3i, WaveCell> baseWave, int grid, int maxAttempts, int maxBacktracks, SeedBox seedBox, boolean multithreading, boolean debug, int workerId) {
        SeedBox childSeedBox = multithreading ? seedBox.child(workerId + "s") : seedBox;
        SeedBox attemptSeedBox = null;
        AtomicReference<Map<Vector3i, WaveCell>> winnerGridTiles = winnerGridTilesMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>());
        AtomicReference<Winner> winner = winnerMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>(null));

        Map<Vector3i, WaveCell> wave = new HashMap<>();
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
                if(winner.get() != null) return null; //Give up LOOSER!

                //Find cell with lowest entropy
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
                            if (change.cell != null) wave.put(change.pos, change.cell);
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

                if (lowestEntropy == null) break; //No collapsible cell found
                if (failed) { failed = false; continue; }

                //Collapse
                var bla = new WaveCellChange(lowestEntropy.position, lowestEntropy);
                lowestEntropy.collapse(randomSupplier);
                bla.cell.possible.remove(lowestEntropy.chosen.tileEntry());
                stack.push(bla);
                collapsedCount++;

                //Propagate to neighbors
                propagate(lowestEntropy, wave, stack, grid);
            }
            if (!failed){ return wave; }
        }

        if (multithreading) {
            if (winner.get() == null && winnerGridTiles.compareAndSet(null, new HashMap<>(wave))) {
                winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt));
            }
            return new HashMap<>(winnerGridTiles.get());
        }

        if(workerId == 1) winner.set(new Winner(workerId, attemptSeedBox, backtracksCount, attempt));
        return wave;
    }

    private static @NonNull Map<Vector3i, WaveCell> getDebugWave(Map<Vector3i, WaveCell> baseWave) {
        Map<Vector3i, WaveCell> wave;
        wave = baseWave.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<Vector3i, WaveCell> e) -> e.getKey().x)
                        .thenComparingInt(e -> e.getKey().z))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        int counter = 0;
        for(Map.Entry<Vector3i, WaveCell> entry : wave.entrySet()){
            if (entry.getValue().isCollapsed()) continue;
            List<TileSet.TileEntry> possibles = new ArrayList<>(entry.getValue().possible);
            entry.getValue().setChosen(possibles.get(counter % possibles.size()), GridTileType.BASIC);
            counter++;
        }
        return wave;
    }

    private static void propagate(WaveCell source, Map<Vector3i, WaveCell> wave, Deque<WaveCellChange> stack, int grid) {
        for (int d = 0; d < 4; d++) {
            Vector3i neighborPos = new Vector3i(source.position).add(dirs[d].clone().scale(grid));
            WaveCell neighbor = wave.get(neighborPos);

            if(stack != null) stack.push(new WaveCellChange(neighborPos, neighbor));
            if (neighbor == null || neighbor.isCollapsed()) continue;

            final String required = source.chosen.tileEntry().getMainRuleSet()[d];

            final int oppositeDir = opposite[d];
            neighbor.possible.removeIf(tileEntry -> !match(tileEntry.getMainRuleSet()[oppositeDir], required));
        }
    }

    public static Map<Vector3i, WaveCell> placeFancyTiles(Map<Vector3i, WaveCell> wave, @NonNull List<TileSet.TileEntry> fancyTileEntries, SeedBox seedBox){
        if (wave == null) return null;
        Map<Vector3i, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        Random randomSupplier = new Random(seedBox.createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries){
                boolean fullMatch = true;
                List<Vector3i> affectedKeys = new ArrayList<>();

                for(var subRuleSet : fancyTileEntry.ruleSets().entrySet()){
                    Vector3i key = waveCellEntry.getKey().clone().add(subRuleSet.getKey().clone());
                    affectedKeys.add(key);
                    if(!fancyWave.containsKey(key)) { fullMatch = false; break;}
                    var chosen= fancyWave.get(key).getChosen();
                    if (chosen == null || chosen.type != GridTileType.BASIC) { fullMatch = false; break; }
                    if (!match(subRuleSet.getValue(),chosen.tileEntry().getMainRuleSet())) { fullMatch = false; break;}
                }
                if(!fullMatch) continue;
                if (randomSupplier.nextDouble(1) > fancyTileEntry.weight()) continue;


                for(var subTiles : fancyTileEntry.getSubTiles()){
                    Vector3i key = waveCellEntry.getKey().clone().add(subTiles.identifierKey().clone());
                    if(match(subTiles.getMainRuleSet(), new String[]{"ö","ö","ö","ö"})) continue;
                    fancyWave.get(key).setChosen(subTiles, GridTileType.FANCY);
                }
                break;
            }
        }
        return fancyWave;
    }

    private static boolean match(String[] a, String[] b){
        return a == null || b == null || a.length == b.length && IntStream.range(0, a.length).allMatch(i -> a[i] == null || b[i] == null || a[i].isEmpty() || b[i].isEmpty() || a[i].equals(b[i]));
    }
    private static boolean match(String a, String b){
        return a == null || b == null || a.isEmpty() || b.isEmpty() || a.equals(b);
    }

    public static class WaveCell {
        Vector3i position;
        LinkedHashSet<TileSet.TileEntry> possible;
        GridTile chosen;

        public GridTile getChosen() { return  chosen; }
        public boolean isCollapsed() { return chosen != null; }
        public int getEntropy() { return possible == null ? 0 : possible.size(); }

        public void collapse(Random randomSupplier) {
            var weightedMap = new WeightedMap<TileSet.TileEntry>();
            possible.forEach(t -> weightedMap.add(t, t.weight()));
            chosen = new GridTile(weightedMap.pick(randomSupplier), position.clone(), GridTileType.BASIC);
            possible = null;
        }

        public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
            chosen = new GridTile(tileEntry, position.clone(), type);
            possible = null;
        }

        WaveCell(Vector3i pos, LinkedHashSet<TileSet.TileEntry> possible) {
            this.position = pos;
            this.possible = possible;
        }


        WaveCell(WaveCell other) {
            this.position = other.position == null ? null : other.position.clone();
            this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
            this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.positionOffset(), other.chosen.type);
        }
    }

    private record WaveCellChange(Vector3i pos, WaveCell cell) {
        private WaveCellChange(Vector3i pos, WaveCell cell) {
            this.pos = pos;
            this.cell = cell == null ? null : new WaveCell(cell);
        }
    }


    public record GridTile(TileSet.TileEntry tileEntry, Vector3i positionOffset, GridTileType type) {
        private void appendLines(StringBuilder[] builders, String pathKey, int width) {
            String[] k = Arrays.stream(tileEntry.getMainRuleSet())
                    .map(s -> (s == null || s.isEmpty()) ? "?" : s)
                    .toArray(String[]::new);
width++;
            k[0] = padRight(k[0], width);
            k[1] = padCenter(k[1], width * 2);
            k[2] = padLeft(k[2], width);
            k[3] = padCenter(k[3], width * 2);

            char[] c = corners.get(type);
            int m = pathKey == null || pathKey.isEmpty() ? 0 :
                    (k[0].equals(pathKey) ? 1 : 0) |
                            (k[1].equals(pathKey) ? 2 : 0) |
                            (k[2].equals(pathKey) ? 4 : 0) |
                            (k[3].equals(pathKey) ? 8 : 0);

            String rot = rotationNumbers[tileEntry.rot()];

            builders[0].append(c[0]).append(" ").append(k[1]).append(" ").append(c[1]).append(" ");
            builders[1].append(k[0]).append(" ").append(p[m]).append(rot).append(k[2]).append(" ");
            builders[2].append(c[2]).append(" ").append(k[3]).append(" ").append(c[3]).append(" ");
        }
        private static final String[] rotationNumbers = {"₀", "₁", "₂", "₃"};
        private static final Map<GridTileType, char[]> corners = Map.of(
                GridTileType.BASIC,  new char[]{'┌','┐','└','┘'},
                GridTileType.POI,   new char[]{'╔','╗','╚','╝'},
                GridTileType.FANCY, new char[]{'┏','┓','┗','┛'}
        );
        private static final char[] p = {
                ' ',   // 0b0000 = no connections
                '←',   // 0b0001 = W
                '↑',   // 0b0010 = N
                '┘',   // 0b0011 = N+W
                '→',   // 0b0100 = E
                '─',   // 0b0101 = E+W
                '└',   // 0b0110 = N+E
                '┴',   // 0b0111 = N+E+W
                '↓',   // 0b1000 = S
                '┐',   // 0b1001 = S+W
                '│',   // 0b1010 = N+S
                '┤',   // 0b1011 = N+S+W
                '┌',   // 0b1100 = E+S
                '┬',   // 0b1101 = N+E+S
                '├',   // 0b1110 = E+S+W
                '┼'    // 0b1111 = N+E+S+W
        };

        private static String padRight(String s, int width) {
            if (s.length() >= width) return s;
            return s + " ".repeat(width - s.length());
        }
        private static String padLeft(String s, int width) {
            if (s.length() >= width) return s;
            return " ".repeat(width - s.length()) + s;
        }
        private static String padCenter(String s, int width) {
            if (s.length() >= width) return s;
            int total = width - s.length();
            int left = total / 2;
            int right = total - left - 1;
            return " ".repeat(left) + s + " ".repeat(right);
        }
    }

    public static @NonNull Map<Vector3d, Prop> loadPrefabProps(PropDistributionAsset.Argument argument, int grid, List<GridTile> gridTiles) {
        Map<Vector3d, Prop> gridProps = new LinkedHashMap<>();
        Vector3i[] anchorOffsets = getAnchorOffsets(grid);
        for (var gridTile : gridTiles) {
            if (gridTile == null) continue;
            if (gridTile.tileEntry().weightedPathAssets() == null || gridTile.tileEntry().weightedPathAssets().size() == 0) continue;
            Prop prop = new PrefabProp(gridTile.tileEntry().weightedPathAssets(), argument.materialCache, argument.parentSeed);
            Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(gridTile.tileEntry().rotation(), Rotation.None, Rotation.None), argument.materialCache);
            Vector3i offset = gridTile.positionOffset().clone().add(gridTile.tileEntry().getOffset().add(anchorOffsets[gridTile.tileEntry().rot()].clone()));
            gridProps.put(offset.toVector3d(), rotatedProp);
        }
        return gridProps;
    }

    public static void sendDebugLog(List<GridTile> gridTiles, int grid, String pathKey, SeedBox seedBox) {
        Winner winner = winnerMap.get(seedBox.toString()).get(); //Maybe need to add checks
        String generatedString = WFC.generateString(gridTiles, pathKey);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final String message = "Generated "+ gridTiles.size() + " tiles based on " + winner.toString() + " with grid: " + grid + " :\n" + generatedString;
        scheduler.schedule(() -> LoggerUtil.getLogger().info(message), 2, TimeUnit.SECONDS);
    }
    public static String generateString(List<GridTile> gridTiles, String pathKey){
        if (gridTiles == null) return "null";
        var list = new ArrayList<>(gridTiles);
        list.removeIf(Objects::isNull);
        list.sort(Comparator.comparingInt((GridTile gt) -> gt.positionOffset().x).thenComparingInt(gt -> gt.positionOffset().z));
        int maxKeyLenght = getMaxKeyLength(list);
        StringBuilder sb = new StringBuilder();
        StringBuilder[] lines = { new StringBuilder(), new StringBuilder(), new StringBuilder() };

        int lastX = list.getFirst().positionOffset().x;

        for (GridTile gridTile : list) {
            if (lastX != gridTile.positionOffset().x) {
                sb.insert(0, lines[2].toString() + "\n");
                sb.insert(0, lines[1].toString() + "\n");
                sb.insert(0, lines[0].toString() + "\n");

                lines[0].setLength(0);
                lines[1].setLength(0);
                lines[2].setLength(0);

                lastX = gridTile.positionOffset().x;
            }
            gridTile.appendLines(lines, pathKey, maxKeyLenght);
        }

        sb.insert(0, lines[2].toString() + "\n");
        sb.insert(0, lines[1].toString() + "\n");
        sb.insert(0, lines[0].toString() + "\n");

        return sb.toString();
    }

    private static int getMaxKeyLength(List<GridTile> list) {
        if (list == null) return 1;
        return list.stream()
                .filter(tile -> tile != null && tile.tileEntry() != null && tile.tileEntry().ruleSets() != null)
                .flatMap(tile -> tile.tileEntry().ruleSets().values().stream())
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .max()
                .orElse(1);
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
    public static int getGrid(List<Vector3d> gridPositions) {
        return (int) Math.round(gridPositions
                .stream()
                .flatMap(p1 -> gridPositions.stream().map(p2 -> p1.distanceTo(p2)))
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

    @Nonnull
    public static PropDistributionAsset.Argument argumentFrom(@Nonnull PropAsset.Argument argument) {
        return new PropDistributionAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId);
    }
}
