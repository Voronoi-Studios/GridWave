package com.png.GridWaveCore.SeedNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class ConstantSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<ConstantSeedAsset> CODEC = BuilderCodec.builder(
                    ConstantSeedAsset.class, ConstantSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, value) -> asset.seed = value, value -> value.seed)
            .add()
            .build();
    private String seed;

    @Override
    public String build() { return this.seed; }
}
