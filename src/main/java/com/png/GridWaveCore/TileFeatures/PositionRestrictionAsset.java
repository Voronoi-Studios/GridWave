package com.png.GridWaveCore.TileFeatures;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;

import javax.annotation.Nonnull;

public class PositionRestrictionAsset extends TileFeatureAsset {
    @Nonnull
    public static final BuilderCodec<PositionRestrictionAsset> CODEC = BuilderCodec.builder(
                    PositionRestrictionAsset.class, PositionRestrictionAsset::new, TileFeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Pos", Vector3i.CODEC, true), (asset, value) -> asset.pos = value, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public Vector3i pos;
    public Rotation rot = Rotation.None;

    @Override
    public TileFeatureType getTileFeatureType() {
        return TileFeatureType.POSITION_RESTRICTION;
    }
}
