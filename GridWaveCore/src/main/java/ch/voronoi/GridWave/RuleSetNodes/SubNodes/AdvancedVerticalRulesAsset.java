package ch.voronoi.GridWave.RuleSetNodes.SubNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.VerticalRules;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class AdvancedVerticalRulesAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, AdvancedVerticalRulesAsset>> {
    @Nonnull
    public static final AssetBuilderCodec<String, AdvancedVerticalRulesAsset> CODEC = AssetBuilderCodec.builder(
                AdvancedVerticalRulesAsset.class,
                AdvancedVerticalRulesAsset::new,
                Codec.STRING,
                (asset, id) -> asset.id = id,
                config -> config.id,
                (config, data) -> config.data = data,
                config -> config.data
            )
            .append(new KeyedCodec<>("VerticalRulesProvider", VerticalRules.CODEC, true), (asset, v) -> asset.verticalRulesProvider = v, asset -> asset.verticalRulesProvider)
            .add()
            .append(new KeyedCodec<>("VerticalRulesReceiver", VerticalRules.CODEC, true), (asset, v) -> asset.verticalRulesReceiver = v, asset -> asset.verticalRulesReceiver)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;

    public VerticalRules verticalRulesProvider;
    public VerticalRules verticalRulesReceiver;

    @Override
    public String getId() {
        return id;
    }
}
