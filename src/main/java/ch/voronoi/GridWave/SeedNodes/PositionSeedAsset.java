package ch.voronoi.GridWave.SeedNodes;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;

import javax.annotation.Nonnull;

public class PositionSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<PositionSeedAsset> CODEC = BuilderCodec.builder(
                    PositionSeedAsset.class, PositionSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .build();

    @Override
    public String build(IAlgoAsset algoAsset) {
        return algoAsset.getAnchorPosition(null).toString() + "s";
    }
}
