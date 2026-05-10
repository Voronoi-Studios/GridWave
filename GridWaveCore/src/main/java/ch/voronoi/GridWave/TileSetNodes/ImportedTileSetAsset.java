package ch.voronoi.GridWave.TileSetNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.List;

public class ImportedTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<ImportedTileSetAsset> CODEC = BuilderCodec.builder(
                    ImportedTileSetAsset.class, ImportedTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importName = k, k -> k.importName)
            .add()
            .build();
    private String importName = "";

    @Override
    public List<TileSet> build(@Nonnull TileSetAsset.Argument argument) {
        if (this.importName != null && !this.importName.isEmpty()) {
            TileSetAsset.Exported exported = getExportedAsset(this.importName);
            if(exported != null && exported.asset != null){
                return exported.asset.build(argument);
            }
            else {
                HytaleLogger.getLogger().atWarning().log("An exported TileSet with this name does not exist: " + this.importName);
                return null;
            }
        }
        return null;
    }
}
