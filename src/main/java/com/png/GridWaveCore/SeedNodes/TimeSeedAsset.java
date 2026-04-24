package com.png.GridWaveCore.SeedNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class TimeSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<TimeSeedAsset> CODEC = BuilderCodec.builder(
                    TimeSeedAsset.class, TimeSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Interval", Codec.INTEGER, true), (asset, value) -> asset.interval = value, value -> value.interval)
            .add()
            .append(new KeyedCodec<>("Unit", new CEnumCodec<>(TimeUnit.class)), (asset, value) -> asset.unit = value, value -> value.unit)
            .add()
            .build();
    private Integer interval = 1;
    private TimeUnit unit = TimeUnit.DAYS;

    @Override
    public String build(IAlgoAsset algoAsset) {
        long now = unit.toChronoUnit().between(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC), LocalDateTime.now());
        long bucket = now / interval;

        return bucket + "t";
    }
}
