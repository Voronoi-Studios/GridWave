package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;

public enum ElevationDirection {
    None,
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NorthWest,
    Up,
    Down;

    public static final Codec<ElevationDirection> CODEC = new EnumCodec<>(ElevationDirection.class);
}
