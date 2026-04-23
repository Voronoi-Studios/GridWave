package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.png.GridWaveCore.TileNodes.TileSet;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PositionRestrictionAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PositionRestrictionAsset> CODEC = BuilderCodec.builder(
                    PositionRestrictionAsset.class, PositionRestrictionAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Pos", Vector3i.CODEC, true), (asset, value) -> asset.pos = value, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public Vector3i pos;
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries) {
        if(super.skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileSet.TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        TileSet.offsetTileEntry(tileEntry, pos);
        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
