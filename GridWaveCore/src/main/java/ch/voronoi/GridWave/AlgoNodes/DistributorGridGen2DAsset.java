package ch.voronoi.GridWave.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ClustersPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.ScalerPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.SquareGrid2dPositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class DistributorGridGen2DAsset extends PositionProviderAsset {
    @Nonnull
    public static final BuilderCodec<DistributorGridGen2DAsset> CODEC = BuilderCodec.builder(DistributorGridGen2DAsset.class, DistributorGridGen2DAsset::new, PositionProviderAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Distributor", PositionProviderAsset.CODEC, true),(asset, value) -> asset.distributorPositionProviderAsset = value, asset -> asset.distributorPositionProviderAsset)
            .add()
            .append(new KeyedCodec<>("OffsetX", Codec.INTEGER, true), (asset, v) -> asset.offsetX = v, asset -> asset.offsetX)
            .add()
            .append(new KeyedCodec<>("OffsetZ", Codec.INTEGER, true), (asset, v) -> asset.offsetZ = v, asset -> asset.offsetZ)
            .add()
            .append(new KeyedCodec<>("RepeatX", Codec.INTEGER, true), (asset, v) -> asset.repeatX = v, asset -> asset.repeatX)
            .add()
            .append(new KeyedCodec<>("RepeatZ", Codec.INTEGER, true), (asset, v) -> asset.repeatZ = v, asset -> asset.repeatZ)
            .add()
            .append(new KeyedCodec<>("Centered", Codec.BOOLEAN, true), (asset, v) -> asset.centeredOnPosition = v, asset -> asset.centeredOnPosition)
            .add()
            .build();

    private PositionProviderAsset distributorPositionProviderAsset = new ListPositionProviderAsset();

    private int offsetX = 1;
    private int offsetZ = 1;

    private int repeatX = 1;
    private int repeatZ = 1;

    private boolean centeredOnPosition;

    @Override
    public PositionProvider build(@Nonnull Argument argument) {
        var clusterPositions = new ScalerPositionProvider(new Vector3d(offsetX, 0, offsetZ),new SquareGrid2dPositionProvider());
        Bounds3d bounds3d = GridGen.createBounds(Vector3i.ZERO.clone(), new Vector3i(offsetX,0, offsetZ), new Vector3i(repeatX, 0,repeatZ), centeredOnPosition);
        return new ClustersPositionProvider(clusterPositions,distributorPositionProviderAsset.build(argument),bounds3d);
        //return new GridGen(pos.clone(), new Vector3i(offsetX,0, offsetZ), new Vector3i(repeatX, 0,repeatZ), centeredOnPosition);
    }
}
