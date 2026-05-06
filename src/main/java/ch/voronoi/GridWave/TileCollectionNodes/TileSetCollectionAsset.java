package ch.voronoi.GridWave.TileCollectionNodes;

import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TileSetCollectionAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, TileSetCollectionAsset>> {
    @Nonnull
    public static final AssetCodecMapCodec<String, TileSetCollectionAsset> CODEC = new AssetCodecMapCodec<>(Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data);
    @Nonnull
    private static final Map<String, TileSetCollectionAsset.Exported> exportedNodes = new ConcurrentHashMap<>();
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(TileSetCollectionAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<TileSetCollectionAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(TileSetCollectionAsset.class)
            .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
            .add()
            .afterDecode(asset -> {
                if (asset.exportName != null && !asset.exportName.isEmpty()) {
                    if (exportedNodes.containsKey(asset.exportName)) {
                        LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
                    }

                    TileSetCollectionAsset.Exported exported = new TileSetCollectionAsset.Exported();
                    exported.asset = asset;
                    exportedNodes.put(asset.exportName, exported);
                    LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
                }
            })
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private String exportName = "";

    public abstract List<TileSet.TileEntry> build(@Nonnull TileSetAsset.Argument argument);

    public static TileSetCollectionAsset.Exported getExportedAsset(@Nonnull String name) {
        return exportedNodes.get(name);
    }

    public String getId() {
        return this.id;
    }

    public abstract TileSetAsset[] getTileSetAssets();

    public static class Exported {
        public TileSetCollectionAsset asset;
    }
}
