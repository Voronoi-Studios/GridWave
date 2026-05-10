package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.POIInfo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;

import static ch.voronoi.GridWave.AlgoNodes.GridWave.dirs;
import static ch.voronoi.GridWave.AlgoNodes.Helper.Match.oppositeDirection;

public class PathKeyFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PathKeyFeatureAsset> CODEC = BuilderCodec.builder(
                    PathKeyFeatureAsset.class, PathKeyFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("PathKey", Codec.STRING, true), (asset, value) -> asset.pathKey = value, asset -> asset.pathKey)
            .add()
            .append(new KeyedCodec<>("CleanIsolated", Codec.BOOLEAN, true), (asset, value) -> asset.cleanIsolated = value, asset -> asset.cleanIsolated)
            .add()
            .build();

    private String pathKey = "";
    private boolean cleanIsolated;

    @Override
    public boolean FinalCheck(Map<Vector3i, WaveCell> baseWave, int participantNumber, TileSetAsset.Argument argument) {
        if(skip()) return true;
        final long count = baseWave.values().stream().flatMap(x -> x.getChosen().connectedPOIs().stream()).map(y -> y.key).distinct().count();

        FloodFill(baseWave, argument);

        boolean check = baseWave.values().stream().anyMatch(x -> x.connectedPOIs.size() >= count);
        if(check && cleanIsolated) baseWave.entrySet().stream()
                .filter(x -> x.getValue().getChosen().type() != GridTileType.POI)
                .filter(x -> x.getValue().connectedPOIs.isEmpty())
                .forEach(x -> x.setValue(new WaveCell(x.getKey(),x.getKey(),new LinkedHashSet<>())));
        return check;
    }

    private void FloodFill(Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) {
        // Breadth-First Search from every POI cell outward, unioning connected cells into the same connectedPOIs set
        Deque<WaveCell> queue = new ArrayDeque<>();

        // Seed the queue with all POI cells (they already have connectedPOIs populated)
        baseWave.values().stream()
                .filter(cell -> !cell.connectedPOIs.isEmpty())
                .forEach(queue::add);

        while (!queue.isEmpty()) {
            WaveCell source = queue.poll();

            IntStream.range(0, 4).forEach(dir -> {
                Vector3i neighborPos = new Vector3i(source.getGridPosition()).add(dirs[dir].clone().scale(argument.algoAsset.getGrid()));
                WaveCell neighbor = baseWave.get(neighborPos);

                if (neighbor == null || !neighbor.isCollapsed()) return;

                RuleCombo neighborRuleSet = neighbor.getChosen().tileEntry().getMainRuleSet();
                String[][] neighborHorizontalRuleSetArrays = neighborRuleSet.providerRuleSet().horizontalRules().getArrays();
                if (!Arrays.stream(neighborHorizontalRuleSetArrays[oppositeDirection[dir]]).toList().contains(pathKey)) return;

                // Find the minimum distance from source's POIInfos and add 1
                int newDistance = source.connectedPOIs.stream()
                        .mapToInt(info -> info.distance)
                        .min().orElse(0) + 1;

                boolean updated = false;

                for (POIInfo sourcePOI : source.connectedPOIs) {
                    POIInfo existing = neighbor.connectedPOIs.stream()
                            .filter(i -> i.key.equals(sourcePOI.key))
                            .findFirst().orElse(null);

                    if (existing == null) {
                        // New POI connection discovered
                        POIInfo newInfo = new POIInfo(sourcePOI.key);
                        newInfo.distance = newDistance;
                        neighbor.connectedPOIs.add(newInfo);
                        updated = true;
                    } else if (newDistance < existing.distance) {
                        // Found a shorter path to an already known POI
                        existing.distance = newDistance;
                        updated = true;
                    }
                }

                // Only re-queue neighbor if something changed
                if (updated) queue.add(neighbor);
            });
        }
    }

    public String[] getPathKeys() { return pathKey.split(","); }
}
