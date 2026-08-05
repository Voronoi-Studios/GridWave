package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.SeedNodes.ConstantSeedAsset;
import ch.voronoi.GridWave.SeedNodes.SeedAsset;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.GridGen.CustomBoundsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class RandomRestrainerFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<RandomRestrainerFeatureAsset> CODEC = BuilderCodec.builder(
                    RandomRestrainerFeatureAsset.class, RandomRestrainerFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, value) -> asset.positionProviderAsset = value, asset -> asset.positionProviderAsset)
            .add()
            .append(new KeyedCodec<>("Bounds", CustomBoundsAsset.CODEC, false), (asset, value) -> asset.customBoundsAsset = value, asset -> asset.customBoundsAsset)
            .add()
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public PositionProviderAsset positionProviderAsset;
    public CustomBoundsAsset customBoundsAsset = null;
    private SeedAsset seed = new ConstantSeedAsset();
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileEntry> tileEntries, TileSetAsset.Argument argument) {
        if(skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId, argument.threadBridge));
        Bounds3i bounds3i = new Bounds3i(Vector3iUtil.MIN, Vector3iUtil.MAX);
        Vector3ic grid = argument.algoAsset.getGrid();
        if(customBoundsAsset != null) bounds3i = customBoundsAsset.build().clone().offset(grid.x(), 0, grid.z());
        List<Vector3dc> gridPositions = GridWave.getPositions(positionProvider, bounds3i,Integer.MAX_VALUE);
        if (!gridPositions.isEmpty() && seed != null) {
            SeedBox seedBox = argument.parentSeed.child(seed.build(argument.algoAsset));
            Random randomSupplier = new Random(seedBox.createSupplier().get());
            Vector3dc random = gridPositions.get(randomSupplier.nextInt(gridPositions.size()));
            tileEntry = tileEntry.restrain(Vector3dUtil.toVector3i((Vector3d)random));
        }
        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
