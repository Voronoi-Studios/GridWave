package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.IntegerBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class BoundsConverterAsset extends CustomBoundsAsset {
    @Nonnull
    public static final BuilderCodec<BoundsConverterAsset> CODEC = AssetBuilderCodec.builder(BoundsConverterAsset.class,BoundsConverterAsset::new,CustomBoundsAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Bounds", IntegerBounds3dAsset.CODEC, true), (asset, v) -> asset.bounds = v, asset -> asset.bounds)
            .add()
            .build();

    private IntegerBounds3dAsset bounds = new IntegerBounds3dAsset();

    @Nonnull
    public Bounds3i build() {
        return bounds.build();
    }
}
