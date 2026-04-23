package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class OverlapTileAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<OverlapTileAsset> CODEC = BuilderCodec.builder(
                    OverlapTileAsset.class, OverlapTileAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();
}
