package ch.voronoi.GridWave.Utils.MirrorNode.Helper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.Axis;

public enum MirrorDirection {
    None,
    X,
    Y,
    Z;

    public static final Codec<MirrorDirection> CODEC = new EnumCodec<>(MirrorDirection.class);

    public Axis toAxis() {
        return switch (this){
            case X -> Axis.X;
            case Y -> Axis.Y;
            case Z -> Axis.Z;
            default -> null;
        };
    }
}
