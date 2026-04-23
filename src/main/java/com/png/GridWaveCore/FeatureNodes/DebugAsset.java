package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.png.GridWaveCore.AlgoNodes.Helper.GridTileType;
import com.png.GridWaveCore.AlgoNodes.Helper.WaveCell;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import org.jspecify.annotations.NonNull;

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
     * bottom left to top right and loops through all tile variants (rotations)*/
    public boolean WFCReplacer(Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        sortByXThenZ(baseWave);
        int counter = 0;
        for(Map.Entry<Vector3i, WaveCell> entry : baseWave.entrySet()){
            if (entry.getValue().isCollapsed()) continue;
            List<TileSet.TileEntry> possibles = new ArrayList<>(entry.getValue().possible);
            entry.getValue().setChosen(possibles.get(counter % possibles.size()), GridTileType.BASIC);
            counter++;
        }
        return false;
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
