package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum BorderType {
    InnerBorder,
    OuterBorder;

    public static final Codec<BorderType> CODEC = new EnumCodec<>(BorderType.class);
}
