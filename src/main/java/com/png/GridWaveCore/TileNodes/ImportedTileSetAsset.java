package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;

public class ImportedTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<ImportedTileSetAsset> CODEC = BuilderCodec.builder(
                    ImportedTileSetAsset.class, ImportedTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.name = k, k -> k.name)
            .add()
            .build();
    private String name = "";

    @Override
    public TileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        if (super.isSkipped()) {
            return null;
        } else if (this.name != null && !this.name.isEmpty()) {
            TileSetAsset exportedAsset = TileSetAsset.getExportedAsset(this.name);
            return exportedAsset == null ? null : exportedAsset.build(argument, grid);
        } else {
            HytaleLogger.getLogger().atWarning().log("An exported Seed with the name does not exist: " + this.name);
            return null;
        }
    }
}
