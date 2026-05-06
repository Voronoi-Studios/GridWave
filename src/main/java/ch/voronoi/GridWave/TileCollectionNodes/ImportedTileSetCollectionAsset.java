package ch.voronoi.GridWave.TileCollectionNodes;

import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.List;

public class ImportedTileSetCollectionAsset extends TileSetCollectionAsset {
    @Nonnull
    public static final BuilderCodec<ImportedTileSetCollectionAsset> CODEC = BuilderCodec.builder(
                    ImportedTileSetCollectionAsset.class, ImportedTileSetCollectionAsset::new, TileSetCollectionAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importName = k, k -> k.importName)
            .add()
            .build();
    private String importName = "";

    @Override
    public List<TileSet.TileEntry> build(@Nonnull TileSetAsset.Argument argument) {
        if (this.importName != null && !this.importName.isEmpty()) {
            TileSetCollectionAsset.Exported exported = getExportedAsset(this.importName);
            if(exported != null && exported.asset != null){ return exported.asset.build(argument); }
            else {
                HytaleLogger.getLogger().atWarning().log("An exported TileCollection with this does not exist: " + this.importName);
                return null;
            }
        }
        return null;
    }

    @Override
    public TileSetAsset[] getTileSetAssets() {
        if (this.importName != null && !this.importName.isEmpty()) {
            TileSetCollectionAsset.Exported exported = getExportedAsset(this.importName);
            if(exported != null && exported.asset != null){ return exported.asset.getTileSetAssets(); }
            else {
                HytaleLogger.getLogger().atWarning().log("An exported TileCollection with this does not exist: " + this.importName);
                return new TileSetAsset[0];
            }
        }
        return new TileSetAsset[0];
    }
}
