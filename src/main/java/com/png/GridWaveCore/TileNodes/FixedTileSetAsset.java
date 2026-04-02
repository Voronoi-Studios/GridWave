package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;

import javax.annotation.Nonnull;

public class FixedTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<FixedTileSetAsset> CODEC = BuilderCodec.builder(FixedTileSetAsset.class, FixedTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC )
            .append(new KeyedCodec<>("TileSet", TileSetAsset.CODEC, true), (asset, value) -> asset.tileSetAsset = value, asset -> asset.tileSetAsset)
            .add()
            .append(new KeyedCodec<>("Pos", Vector3i.CODEC, true), (asset, value) -> asset.pos = value, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    private TileSetAsset tileSetAsset;
    private Vector3i pos;
    private Rotation rot = Rotation.None;

    @Override
    public FixedTileSet build(@Nonnull TileSetAsset.Argument argument, int grid){
        TileSet tileSet = tileSetAsset.build(argument, grid);
        return new FixedTileSet(tileSet,pos == null ? Vector3i.ZERO.clone() : pos, rot.getDegrees() / 90); //Round Pos to grid?
    }
}
