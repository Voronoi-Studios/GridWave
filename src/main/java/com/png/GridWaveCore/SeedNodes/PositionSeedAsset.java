package com.png.GridWaveCore.SeedNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;

import javax.annotation.Nonnull;
import java.util.Random;

public class PositionSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<PositionSeedAsset> CODEC = BuilderCodec.builder(
                    PositionSeedAsset.class, PositionSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .build();

    @Override
    public String build(IAlgoAsset algoAsset) {
        return algoAsset.getAnchorPosition(null).toString() + "s";
    }
}
