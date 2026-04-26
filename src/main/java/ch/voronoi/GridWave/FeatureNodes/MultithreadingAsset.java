package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class MultithreadingAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<RestrainerAsset> CODEC = BuilderCodec.builder(
                    RestrainerAsset.class, RestrainerAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();
}
