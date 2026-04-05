package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class AdvancedRuleSetAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AdvancedRuleSetAsset>> {

    @Nonnull
    public static final AssetBuilderCodec<String, AdvancedRuleSetAsset> CODEC = AssetBuilderCodec.builder(AdvancedRuleSetAsset.class, AdvancedRuleSetAsset::new, Codec.STRING,
                    (asset, id) -> asset.id = id, config -> config.id,(config, data) -> config.data = data, config -> config.data)
            .append(new KeyedCodec<>("Provider RuleSets", RuleSet.CODEC), (op, val) -> op.providerRuleSet = val, op -> op.providerRuleSet)
            .documentation("Who am I, defines what each edge's keys are")
            .add()
            .append(new KeyedCodec<>("Receiver RuleSets", RuleSet.CODEC), (op, val) -> op.recieverRuleSet = val, op -> op.recieverRuleSet)
            .documentation("Who do I like, defines to what keys (provider) this tile can connect to")
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private RuleSet providerRuleSet;
    private RuleSet recieverRuleSet;

    @Override
    public String getId() {
        return id;
    }

    public String[] build() {
        String[][] rules = providerRuleSet.getRuleSets();
        return new String[]{
                rules[0][0],
                rules[1][0],
                rules[2][0],
                rules[3][0]
        };
    }
}