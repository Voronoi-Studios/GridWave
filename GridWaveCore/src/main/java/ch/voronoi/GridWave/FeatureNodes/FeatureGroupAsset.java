package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FeatureGroupAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<FeatureGroupAsset> CODEC = BuilderCodec.builder(FeatureGroupAsset.class, FeatureGroupAsset::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Features", new ArrayCodec<>(FeatureAsset.CODEC, FeatureAsset[]::new), true), (asset, value) -> asset.featureAssets = value, asset -> asset.featureAssets)
            .add()
            .build();

    private FeatureAsset[] featureAssets = new FeatureAsset[0];

    @Override
    public List<FeatureAsset> build() {
        List<FeatureAsset> combinedFeatureAssets = new ArrayList<>();
        for(var featureAsset : featureAssets){
            combinedFeatureAssets.addAll(featureAsset.build());
        }
        return combinedFeatureAssets;
    }
}
