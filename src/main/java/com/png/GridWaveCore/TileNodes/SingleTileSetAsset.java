package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.AlgoNodes.RuleSetAsset;
import com.png.GridWaveCore.UnusedNodes.CPrefabPropAsset;

import javax.annotation.Nonnull;
import java.util.List;

public class SingleTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<SingleTileSetAsset> CODEC = BuilderCodec.builder(SingleTileSetAsset.class, SingleTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSetAsset.CODEC, true), (asset, value) -> asset.ruleSetAsset = value, asset -> asset.ruleSetAsset)
            .add()
            .append(new KeyedCodec<>("WeightedPrefabPaths", new ArrayCodec<>(CPrefabPropAsset.CWeightedPathAsset.CODEC, CPrefabPropAsset.CWeightedPathAsset[]::new), true),
                    (asset, v) -> asset.weightedPrefabPathAssets = v,
                    asset -> asset.weightedPrefabPathAssets
            ).add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .append(new KeyedCodec<>("AutoRot", Codec.BOOLEAN, true), (asset, value) -> asset.autoRot = value, asset -> asset.autoRot)
            .add()
            .build();
    private RuleSetAsset ruleSetAsset = new RuleSetAsset();
    private CPrefabPropAsset.CWeightedPathAsset[] weightedPrefabPathAssets = new CPrefabPropAsset.CWeightedPathAsset[0];
    private double weight = 1;
    private boolean autoRot = true;

    @Nonnull
    @Override
    public SingleTileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();

        for (CPrefabPropAsset.CWeightedPathAsset pathAsset : this.weightedPrefabPathAssets) {
            List<IPrefabBuffer> pathPrefabs = this.loadPrefabBuffersFrom(pathAsset.path);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
                prefabWeightedMap.add(pathPrefabs, pathAsset.weight);
            }
        }

        return new SingleTileSet(prefabWeightedMap,ruleSetAsset.build(), weight, autoRot);
    }
}
