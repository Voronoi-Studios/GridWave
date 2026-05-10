package ch.voronoi.GridWave.RuleSetNodes.SubNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.ElevationRules;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class ElevationRulesAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ElevationRulesAsset>> {
    @Nonnull
    public static final AssetBuilderCodec<String, ElevationRulesAsset> CODEC = AssetBuilderCodec.builder(
                    ElevationRulesAsset.class,
                    ElevationRulesAsset::new,
                    Codec.STRING,
                    (asset, id) -> asset.id = id,
                    config -> config.id,
                    (config, data) -> config.data = data,
                    config -> config.data
            )
            .append(new KeyedCodec<>("ElevationRules", ElevationRules.CODEC, true), (asset, v) -> asset.elevationRules = v, asset -> asset.elevationRules)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;

    public ElevationRules elevationRules;

    @Override
    public String getId() {
        return id;
    }
}
