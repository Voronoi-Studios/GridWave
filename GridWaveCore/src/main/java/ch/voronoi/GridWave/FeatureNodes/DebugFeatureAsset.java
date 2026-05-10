package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.AttemptBehavior;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.TileSetNodes.TileSet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DebugFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<DebugFeatureAsset> CODEC = BuilderCodec.builder(
                    DebugFeatureAsset.class, DebugFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("DebugGrid", Codec.BOOLEAN), (asset, v) -> asset.debugGrid = v, asset -> asset.debugGrid)
            .add()
            .append(new KeyedCodec<>("LimitSteps", Codec.BOOLEAN), (asset, v) -> asset.limitSteps = v, asset -> asset.limitSteps)
            .add()
            .append(new KeyedCodec<>("MaxSteps", Codec.INTEGER), (asset, v) -> asset.maxSteps = v, asset -> asset.maxSteps)
            .add()
            .build();

    private boolean debugGrid;
    private boolean limitSteps;
    private int maxSteps;

    @Override
    public void BeforeWFC(AttemptBehavior attemptBehavior, TileSetAsset.Argument argument) {
        if(limitSteps) attemptBehavior.maxCollapsedCount = maxSteps;
    }

    /** Generates a simplified wave for testing purposes, chronologically collapsing all tiles
     * bottom left to top right and loops through all tile variants (rotations)
     * @return if it had replaced the baseWave
     * */
    @Override
    public boolean WFCReplacer(Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) {
        if(skip() || !debugGrid) return false;
        sortByXThenZ(baseWave);
        int counter = 0;
        for(Map.Entry<Vector3i, WaveCell> entry : baseWave.entrySet()){
            if (entry.getValue().isCollapsed()) continue;
            List<TileSet.TileEntry> possibles = new ArrayList<>(entry.getValue().possible);
            entry.getValue().setChosen(possibles.get(counter % possibles.size()), GridTileType.BASIC);
            counter++;
        }
        return true;
    }

    private void sortByXThenZ(Map<Vector3i, WaveCell> baseWave) {
        baseWave.entrySet().stream()
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
}
