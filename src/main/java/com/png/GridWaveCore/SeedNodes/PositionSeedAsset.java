package com.png.GridWaveCore.SeedNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import javax.annotation.Nonnull;
import java.util.Random;

public class PositionSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<PositionSeedAsset> CODEC = BuilderCodec.builder(
                    PositionSeedAsset.class, PositionSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Position", Vector3iUtil.CODEC, false), (asset, v) -> asset.pos = v, asset -> asset.pos)
            .add()
            .build();

    private Vector3i pos = new Vector3i(Vector3iUtil.ZERO);

    @Override
    public String build() {
        return pos.toString() + "s";
    }
}
