package ch.voronoi.GridWave.SeedNodes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SeedAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, SeedAsset>> {
    @Nonnull
    public static final AssetCodecMapCodec<String, SeedAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );
    @Nonnull
    private static final Map<String, SeedAsset> exportedNodes = new ConcurrentHashMap<>();
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(SeedAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<SeedAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(SeedAsset.class)
            .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
            .add()
            .afterDecode(asset -> {
                if (asset.exportName != null && !asset.exportName.isEmpty()) {
                    if (exportedNodes.containsKey(asset.exportName)) {
                        LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
                    }

                    exportedNodes.put(asset.exportName, asset);
                    LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
                }
            })
            .build();
    private String id;
    private AssetExtraInfo.Data data;
    private String exportName = "";

    protected SeedAsset() {
    }

    public abstract String build(IAlgoAsset algoAsset);

    public static SeedAsset getExportedAsset(@Nonnull String name) {
        return exportedNodes.get(name);
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void cleanUp() {
    }
}
