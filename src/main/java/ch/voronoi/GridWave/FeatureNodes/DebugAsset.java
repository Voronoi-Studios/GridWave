package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;
import ch.voronoi.GridWave.TileNodes.TileSet;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DebugAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<DebugAsset> CODEC = BuilderCodec.builder(
                    DebugAsset.class, DebugAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();

    /** Generates a simplified wave for testing purposes, chronologically collapsing all tiles
     * bottom left to top right and loops through all tile variants (rotations)
     * @return if it had replaced the baseWave
     * */
    @Override
    public boolean WFCReplacer(Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        if(skip()) return false;
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
