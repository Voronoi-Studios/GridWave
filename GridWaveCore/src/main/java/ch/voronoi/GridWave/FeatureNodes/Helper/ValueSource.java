package ch.voronoi.GridWave.FeatureNodes.Helper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum ValueSource {
    CollapsedPercentage();

    public static final Codec<ValueSource> CODEC = new EnumCodec<>(ValueSource.class);
}
