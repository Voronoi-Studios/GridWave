package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class RuleSetAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, RuleSetAsset>> {

    @Nonnull
    public static final AssetBuilderCodec<String, RuleSetAsset> CODEC = AssetBuilderCodec.builder(
                    RuleSetAsset.class,
                    RuleSetAsset::new,
                    Codec.STRING,
                    (asset, id) -> asset.id = id,
                    config -> config.id,
                    (config, data) -> config.data = data,
                    config -> config.data
            )
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n, t -> t.n)
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e, t -> t.e)
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s, t -> t.s)
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w, t -> t.w)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private String n = "";
    private String e = "";
    private String s = "";
    private String w = "";

    @Override
    public String getId() {
        return id;
    }

    public String[] build() {
        return new String[]{n,e,s,w};
    }
}