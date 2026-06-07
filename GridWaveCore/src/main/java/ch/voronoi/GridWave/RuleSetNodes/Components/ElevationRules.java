package ch.voronoi.GridWave.RuleSetNodes.Components;

import ch.voronoi.GridWave.FeatureNodes.Helper.ElevationDirection;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ElevationRules {
    @Nonnull
    public static final BuilderCodec<ElevationRules> CODEC = BuilderCodec.builder(ElevationRules.class, ElevationRules::new)
            .append(new KeyedCodec<>("ElevationDirection", ElevationDirection.CODEC, true), (asset,v)->asset.elevationDirection =v,asset ->asset.elevationDirection)
            .add()
            .append(new KeyedCodec<>("ElevationChange", Codec.DOUBLE, true), (asset,v)->asset.elevationChange =v,asset ->asset.elevationChange)
            .add()
            .build();

    public ElevationDirection elevationDirection;
    public double elevationChange;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElevationRules other)) return false;

        return elevationDirection.equals(other.elevationDirection)
            && elevationChange == other.elevationChange;
    }
}
