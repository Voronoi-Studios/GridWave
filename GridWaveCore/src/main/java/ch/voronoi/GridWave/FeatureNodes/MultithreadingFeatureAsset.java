package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MultithreadingFeatureAsset extends FeatureAsset {
    private static final ConcurrentHashMap<String, Context> multithreadingCash = new ConcurrentHashMap<>();

    @Nonnull
    public static final BuilderCodec<RestrainerFeatureAsset> CODEC = BuilderCodec.builder(
                    RestrainerFeatureAsset.class, RestrainerFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .build();

    public Context get(TileSetAsset.Argument argument) {
        return multithreadingCash.putIfAbsent(argument.seedBox.toString(), new Context(new AtomicReference<>(), new AtomicInteger()));
    }

    public static record Context(AtomicReference<GridWave.Winner> winner, AtomicInteger participantNumber){

    }
}
