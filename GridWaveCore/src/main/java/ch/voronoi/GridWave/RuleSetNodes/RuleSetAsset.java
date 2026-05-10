package ch.voronoi.GridWave.RuleSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RuleSetAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, RuleSetAsset>> {
    @Nonnull
    public static final AssetCodecMapCodec<String, RuleSetAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );
    @Nonnull
    private static final Map<String, RuleSetAsset> exportedNodes = new ConcurrentHashMap<>();
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(RuleSetAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<RuleSetAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(RuleSetAsset.class).build();
    private String id;
    private AssetExtraInfo.Data data;

    @Override
    public String getId() {
        return id;
    }

    public abstract RuleCombo build();
}