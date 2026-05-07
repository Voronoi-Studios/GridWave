package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import ch.voronoi.GridWave.AlgoNodes.Helper.AttemptBehavior;
import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;

import javax.annotation.Nonnull;
import java.util.List;

public class MultiAttemptAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<MultiAttemptAsset> CODEC = BuilderCodec.builder(
                    MultiAttemptAsset.class, MultiAttemptAsset::new, FeatureAsset.ABSTRACT_CODEC
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
