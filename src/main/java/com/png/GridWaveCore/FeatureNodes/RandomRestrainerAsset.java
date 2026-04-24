package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.png.GridWaveCore.AlgoNodes.GridWave;
import com.png.GridWaveCore.SeedNodes.PositionSeedAsset;
import com.png.GridWaveCore.SeedNodes.SeedAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import com.png.GridWaveCore.TileNodes.TileSetAsset;

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
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public PositionProviderAsset positionProviderAsset;
    private SeedAsset seed = new PositionSeedAsset();
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries, TileSetAsset.Argument argument) {
        if(super.skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileSet.TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
        List<Vector3d> gridPositions = GridWave.getPositions(positionProvider, Integer.MAX_VALUE);
        if (!gridPositions.isEmpty() && seed != null) {
            SeedBox seedBox = argument.parentSeed.child(seed.build(argument.algoAsset));
            Random randomSupplier = new Random(seedBox.createSupplier().get());
            Vector3d random = gridPositions.get(randomSupplier.nextInt(gridPositions.size()));
            TileSet.offsetTileEntry(tileEntry, random.toVector3i());
        }
        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
