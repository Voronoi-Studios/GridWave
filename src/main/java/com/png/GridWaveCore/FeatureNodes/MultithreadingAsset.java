package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;

import javax.annotation.Nonnull;

public class MultithreadingAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PositionRestrictionAsset> CODEC = BuilderCodec.builder(
                    PositionRestrictionAsset.class, PositionRestrictionAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();
}
