package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<MultiTileSetAsset> CODEC = BuilderCodec.builder(MultiTileSetAsset.class, MultiTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSets", new ArrayCodec<>(RuleSetAsset.CODEC, RuleSetAsset[]::new), true), (asset, value) -> asset.ruleSetAssets = value, asset -> asset.ruleSetAssets)
            .add()
            .append(new KeyedCodec<>("PrefabPath", Codec.STRING, true),(asset, v) -> asset.prefabPath = v,asset -> asset.prefabPath)
            .add()
            .append(new KeyedCodec<>("SizeZ", Codec.INTEGER, true), (asset, value) -> asset.zSize = value, asset -> asset.zSize)
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .build();
    private RuleSetAsset[] ruleSetAssets = new SimpleRuleSetAsset[0];
    private String prefabPath = "";
    private int zSize;
    private double weight = 1;

    @Nonnull
    @Override
    public MultiTileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        if(!prefabPath.isEmpty()) {
            List<IPrefabBuffer> pathPrefabs = this.loadPrefabBuffersFrom(prefabPath);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
                prefabWeightedMap.add(pathPrefabs, 1);
            }
        }


        Map<Vector3i, RuleSet.Combo> ruleSets = new HashMap<>();
        Vector3i offset = Vector3i.ZERO.clone();
        for(RuleSetAsset ruleSetAsset : ruleSetAssets){
            ruleSets.put(offset.clone().scale(grid), ruleSetAsset.build());
            offset.z++;
            if(offset.z >= zSize) {
                offset.z = 0;
                offset.x--;
            }
        }

        return new MultiTileSet(prefabWeightedMap, ruleSets, weight, super.getTileFeatureAssets());
    }
}
