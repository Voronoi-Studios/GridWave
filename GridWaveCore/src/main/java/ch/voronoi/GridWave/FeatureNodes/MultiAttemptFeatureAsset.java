package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import ch.voronoi.GridWave.AlgoNodes.Helper.AttemptBehavior;

import javax.annotation.Nonnull;

public class MultiAttemptFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<MultiAttemptFeatureAsset> CODEC = BuilderCodec.builder(
                    MultiAttemptFeatureAsset.class, MultiAttemptFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("MaxBacktracks", Codec.INTEGER, false), (asset, v) -> asset.maxBacktracks = v, asset -> asset.maxBacktracks)
            .add()
            .append(new KeyedCodec<>("MaxAttempts", Codec.INTEGER, false), (asset, v) -> asset.maxAttempts = v, asset -> asset.maxAttempts)
            .add()
            .build();

    public int maxBacktracks = 100;
    public int maxAttempts = 500;

    @Override
    public void BeforeWFC(AttemptBehavior attemptBehavior, TileSetAsset.Argument argument) {
        if(skip()) return;
        attemptBehavior.maxAttempts = maxAttempts;
        attemptBehavior.maxBacktracks = maxBacktracks;
    }

}
