package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.function.predicate.BiFloatPredicate;

public enum ComparisonType {
    GreaterOrEqual((v1, v2) -> v1 >= v2),
    Greater((v1, v2) -> v1 > v2),
    LessOrEqual((v1, v2) -> v1 <= v2),
    Less((v1, v2) -> v1 < v2),
    Equal((v1, v2) -> v1 == v2);

    public static final Codec<ComparisonType> CODEC = new EnumCodec<>(ComparisonType.class);

    private final BiFloatPredicate satisfies;

    private ComparisonType(BiFloatPredicate satisfies) {
        this.satisfies = satisfies;
    }

    public boolean satisfies(float compareTo, float f) {
        return this.satisfies.test(compareTo, f);
    }
}
