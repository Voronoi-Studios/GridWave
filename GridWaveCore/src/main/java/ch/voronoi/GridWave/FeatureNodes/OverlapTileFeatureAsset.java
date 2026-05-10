package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class OverlapTileFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<OverlapTileFeatureAsset> CODEC = BuilderCodec.builder(
                    OverlapTileFeatureAsset.class, OverlapTileFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();
}
