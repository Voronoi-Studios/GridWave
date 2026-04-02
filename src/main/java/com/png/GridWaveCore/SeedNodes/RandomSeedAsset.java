package com.png.GridWaveCore.SeedNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.Random;

public class RandomSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<RandomSeedAsset> CODEC = BuilderCodec.builder(
                    RandomSeedAsset.class, RandomSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, true), (asset, value) -> asset.seedAsset = value, value -> value.seedAsset)
            .add()
            .build();
    private SeedAsset seedAsset;

    @Override
    public String build() {
        return new Random().nextInt(100000000, 999999999) + "s";
    }
}
