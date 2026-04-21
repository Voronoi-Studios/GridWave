package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;
import com.png.GridWaveCore.UnusedNodes.CPrefabPropAsset;

import javax.annotation.Nonnull;
import java.util.List;

public class SingleTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<SingleTileSetAsset> CODEC = BuilderCodec.builder(SingleTileSetAsset.class, SingleTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSetAsset.CODEC, true), (asset, value) -> asset.ruleSetAsset = value, asset -> asset.ruleSetAsset)
            .add()
            .append(new KeyedCodec<>("èrefabPath", Codec.STRING, true),
                    (asset, v) -> asset.prefabPath = v,
                    asset -> asset.prefabPath
            ).add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .append(new KeyedCodec<>("MinimizeVariants", Codec.BOOLEAN, true), (asset, value) -> asset.minimizeVariants = value, asset -> asset.minimizeVariants)
            .add()
            .build();
    private RuleSetAsset ruleSetAsset = new SimpleRuleSetAsset();
    private String prefabPath = "";
    private double weight = 1;
    private boolean minimizeVariants = true;

    @Nonnull
    @Override
    public SingleTileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        List<IPrefabBuffer> pathPrefabs = this.loadPrefabBuffersFrom(prefabPath);
        if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
            prefabWeightedMap.add(pathPrefabs, 1);
        }

        return new SingleTileSet(prefabWeightedMap,ruleSetAsset.build(), weight, minimizeVariants, super.getTileFeatureAssets());
    }
}
