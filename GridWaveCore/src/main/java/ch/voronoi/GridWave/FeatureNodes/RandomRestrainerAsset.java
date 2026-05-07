package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.SeedNodes.ConstantSeedAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.DecimalBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.SeedNodes.SeedAsset;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class RandomRestrainerAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<RandomRestrainerAsset> CODEC = BuilderCodec.builder(
                    RandomRestrainerAsset.class, RandomRestrainerAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, value) -> asset.positionProviderAsset = value, asset -> asset.positionProviderAsset)
            .add()
            .append(new KeyedCodec<>("Bounds", DecimalBounds3dAsset.CODEC, false), (asset, value) -> asset.decimalBounds3dAsset = value, asset -> asset.decimalBounds3dAsset)
            .add()
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public PositionProviderAsset positionProviderAsset;
    public DecimalBounds3dAsset decimalBounds3dAsset = null;
    private SeedAsset seed = new ConstantSeedAsset();
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries, TileSetAsset.Argument argument) {
        if(skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileSet.TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
        Bounds3d bounds3d = new Bounds3d(Vector3d.MIN, Vector3d.MAX);
        if(decimalBounds3dAsset != null) bounds3d = decimalBounds3dAsset.build();
        List<Vector3d> gridPositions = GridWave.getPositions(positionProvider, bounds3d,Integer.MAX_VALUE);
        if (!gridPositions.isEmpty() && seed != null) {
            SeedBox seedBox = argument.parentSeed.child(seed.build(argument.algoAsset));
            Random randomSupplier = new Random(seedBox.createSupplier().get());
            Vector3d random = gridPositions.get(randomSupplier.nextInt(gridPositions.size()));
            tileEntry = TileSet.offsetTileEntry(tileEntry, random.toVector3i());
        }
        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
