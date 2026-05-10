package ch.voronoi.GridWave.RuleSetNodes.SubNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.VerticalRules;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class SimpleVerticalRulesAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SimpleVerticalRulesAsset>> {
    @Nonnull
    public static final AssetBuilderCodec<String, SimpleVerticalRulesAsset> CODEC = AssetBuilderCodec.builder(
                SimpleVerticalRulesAsset.class,
                SimpleVerticalRulesAsset::new,
                Codec.STRING,
                (asset, id) -> asset.id = id,
                config -> config.id,
                (config, data) -> config.data = data,
                config -> config.data
            )
            .append(new KeyedCodec<>("VerticalRules", VerticalRules.CODEC, true), (asset, v) -> asset.verticalRules = v, asset -> asset.verticalRules)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;

    public VerticalRules verticalRules;

    @Override
    public String getId() {
        return id;
    }
}
