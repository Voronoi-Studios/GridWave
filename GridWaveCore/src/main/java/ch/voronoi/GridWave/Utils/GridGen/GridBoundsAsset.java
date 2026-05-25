package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class GridBoundsAsset extends CustomBoundsAsset {
    @Nonnull
    public static final BuilderCodec<GridBoundsAsset> CODEC = AssetBuilderCodec.builder(GridBoundsAsset.class,GridBoundsAsset::new,CustomBoundsAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Position", Vector3i.CODEC, false), (asset, v) -> asset.pos = v, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Offset", Vector3i.CODEC, true), (asset, v) -> asset.offset = v, asset -> asset.offset)
            .add()
            .append(new KeyedCodec<>("Repeat", Vector3i.CODEC, true), (asset, v) -> asset.repeat = v, asset -> asset.repeat)
            .add()
            .append(new KeyedCodec<>("Centered", Codec.BOOLEAN, true), (asset, v) -> asset.centeredOnPosition = v, asset -> asset.centeredOnPosition)
            .add()
            .build();

    private Vector3i pos = new Vector3i();
    private Vector3i offset = new Vector3i();
    private Vector3i repeat = new Vector3i();
    private boolean centeredOnPosition;

    @Nonnull
    public Bounds3i build() {
        return GridGen.createBounds(pos, offset, repeat, centeredOnPosition);
    }
}
