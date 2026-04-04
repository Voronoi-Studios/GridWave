package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.TileNodes.TileSet;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridWave {
    static final Vector3i[] dirs = { Vector3i.NORTH, Vector3i.EAST, Vector3i.SOUTH, Vector3i.WEST };
    static final int[] opposite = {2, 3, 0, 1};

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

    public static Map<Vector3i, WaveCell> performWFC(Map<Vector3i, WaveCell> baseWave, int grid, SeedBox seedBox, int maxAttempts, int maxBacktracks, boolean debug, AtomicInteger winner) {
        if(debug){
            Map<Vector3i, WaveCell> wave = baseWave.entrySet().stream()
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

        Map<Vector3i, WaveCell> wave = new HashMap<>();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Random randomSupplier = new Random(seedBox.child(String.valueOf(attempt)).createSupplier().get());
            wave = new LinkedHashMap<>(baseWave);

            Deque<WaveCellChange> stack = new ArrayDeque<>();
            boolean failed = false;

            int backtracksCount = 0;
            int collapsedCount = 0;
            while (collapsedCount < baseWave.size()) {
                if(winner.get() != -1) return null; //Give up LOOSER!

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

    public static Map<Vector3i, WaveCell> placeFancyTiles(Map<Vector3i, WaveCell> wave, @NonNull List<TileSet.TileEntry> fancyTileEntries, SeedBox seedBox, AtomicInteger winner){
        if (wave == null) return null;
        Map<Vector3i, WaveCell> fancyWave = new LinkedHashMap<>(wave);
        Random randomSupplier = new Random(seedBox.createSupplier().get());
        for(var waveCellEntry : fancyWave.entrySet()){
            for (var fancyTileEntry : fancyTileEntries){
                if(winner.get() != -1) return null;

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

    public static String generateString(List<GridTile> gridTiles, String pathKey){
        if (gridTiles == null) return "null";
        var list = new ArrayList<>(gridTiles);
        list.removeIf(Objects::isNull);
        list.sort(Comparator.comparingInt((GridTile gt) -> gt.positionOffset().x).thenComparingInt(gt -> gt.positionOffset().z));
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
            gridTile.appendLines(lines, pathKey);
        }

        sb.insert(0, lines[2].toString() + "\n");
        sb.insert(0, lines[1].toString() + "\n");
        sb.insert(0, lines[0].toString() + "\n");

        return sb.toString();
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

    public enum GridTileType { BASIC, POI, FANCY }

    public record GridTile(TileSet.TileEntry tileEntry, Vector3i positionOffset, GridTileType type) {
        private void appendLines(StringBuilder[] builders, String pathKey) {
            String[] ruleSet = Arrays.stream(tileEntry.getMainRuleSet())
                    .map(s -> (s == null || s.isEmpty()) ? "?" : s)
                    .toArray(String[]::new);
            char[] c = corners.get(type);

            int mask = pathKey == null || pathKey.isEmpty() ? 0 :
                    (ruleSet[0].equals(pathKey) ? 1 : 0) |
                            (ruleSet[1].equals(pathKey) ? 2 : 0) |
                            (ruleSet[2].equals(pathKey) ? 4 : 0) |
                            (ruleSet[3].equals(pathKey) ? 8 : 0);

            builders[0].append(c[0]).append("  ").append(ruleSet[1]).append("  ").append(c[1]).append(" ");
            builders[1].append(ruleSet[0]).append("  ").append(pathMap[mask]).append(subscriptNumbers[tileEntry.rot()]).append(" ").append(ruleSet[2]).append(" ");
            builders[2].append(c[2]).append("  ").append(ruleSet[3]).append("  ").append(c[3]).append(" ");
        }
        private static final String[] subscriptNumbers = {"₀", "₁", "₂", "₃"};
        private static final Map<GridTileType, char[]> corners = Map.of(
                GridTileType.BASIC,  new char[]{'┌','┐','└','┘'},
                GridTileType.POI,   new char[]{'╔','╗','╚','╝'},
                GridTileType.FANCY, new char[]{'┏','┓','┗','┛'}
        );
        private static final char[] pathMap = {
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
    }
}
