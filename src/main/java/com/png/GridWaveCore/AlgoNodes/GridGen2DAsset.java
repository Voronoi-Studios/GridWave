package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.*;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class GridGen2DAsset extends PositionProviderAsset {
    @Nonnull
    public static final BuilderCodec<GridGen2DAsset> CODEC = BuilderCodec.builder(GridGen2DAsset.class, GridGen2DAsset::new, PositionProviderAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Position", Vector3i.CODEC, false), (asset, v) -> asset.pos = v, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("OffsetX", Codec.INTEGER, true), (asset, v) -> asset.offsetX = v, asset -> asset.offsetX)
            .add()
            .append(new KeyedCodec<>("OffsetZ", Codec.INTEGER, true), (asset, v) -> asset.offsetZ = v, asset -> asset.offsetZ)
            .add()
            .append(new KeyedCodec<>("RepeatX", Codec.INTEGER, true), (asset, v) -> asset.repeatX = v, asset -> asset.repeatX)
            .add()
            .append(new KeyedCodec<>("RepeatZ", Codec.INTEGER, true), (asset, v) -> asset.repeatZ = v, asset -> asset.repeatZ)
            .add()
            .append(new KeyedCodec<>("Centered", Codec.BOOLEAN, true), (asset, v) -> asset.centeredOnPosition = v, asset -> asset.centeredOnPosition)
            .add()
            .build();

    private Vector3i pos = Vector3i.ZERO.clone();

    private int offsetX = 1;
    private int offsetZ = 1;

    private int repeatX = 1;
    private int repeatZ = 1;

    private boolean centeredOnPosition;

    @Override
    public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
        PositionProvider.Context context = new PositionProvider.Context();
        var posProvider1 = new SquareGrid2dPositionProvider();
        var posProvider2 = new ScalerPositionProvider(new Vector3d(),posProvider1);
        Bounds3d bounds3d = new Bounds3d();
        new BoundPositionProvider(posProvider2, bounds3d);
        return new GridGen(pos.clone(), new Vector3i(offsetX,pos.y, offsetZ), new Vector3i(repeatX, 1,repeatZ), centeredOnPosition);
    }
}
