package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.SeedNodes.ConstantSeedAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSetGroupAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.DecimalBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.SeedNodes.SeedAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.IntStream;


public class PropDistributionAlgoAsset extends PropDistributionAsset implements IAlgoAsset {
    @Nonnull
    public static final BuilderCodec<PropDistributionAlgoAsset> CODEC = BuilderCodec.builder(PropDistributionAlgoAsset.class, PropDistributionAlgoAsset::new, PropDistributionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("MaxPositionsCount", Codec.INTEGER, false), (asset, v) -> asset.maxPositionsCount = v, asset -> asset.maxPositionsCount)
            .add()
            .append(new KeyedCodec<>("GridPoints", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset)
            .add()
            .append(new KeyedCodec<>("Grid", Vector3i.CODEC, true), (asset, v) -> asset.grid = v, asset -> asset.grid)
            .add()
            .append(new KeyedCodec<>("Bounds", DecimalBounds3dAsset.CODEC, true), (asset, v) -> asset.decimalBounds3dAsset = v, asset -> asset.decimalBounds3dAsset)
            .add()
            .append(new KeyedCodec<>("POIs", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, v) -> asset.poiTileSetAssets = v, asset -> asset.poiTileSetAssets)
            .add()
            .append(new KeyedCodec<>("BaseTiles", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, v) -> asset.baseTileSetAssets = v, asset -> asset.baseTileSetAssets)
            .add()
            .append(new KeyedCodec<>("FancyTiles", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, v) -> asset.fancyTileSetAssets = v, asset -> asset.fancyTileSetAssets)
            .add()
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("Features", new ArrayCodec<>(FeatureAsset.CODEC, FeatureAsset[]::new), true), (asset, v) -> asset.featureAssets = v, asset -> asset.featureAssets)
            .add()
            .build();

    private int maxPositionsCount = 20;

    private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
    private Vector3i grid = new Vector3i(16,16,16);
    private DecimalBounds3dAsset decimalBounds3dAsset = null;
    private TileSetAsset[] poiTileSetAssets = new TileSetAsset[0];
    private TileSetAsset[] baseTileSetAssets = new TileSetAsset[0];
    private TileSetAsset[] fancyTileSetAssets = new TileSetAsset[0];
    private SeedAsset seed = new ConstantSeedAsset();
    private FeatureAsset[] featureAssets = new FeatureAsset[0];

    @Override
    public Vector3i getGrid() { return grid; }
    @Override
    public int getMaxPositionsCount() { return maxPositionsCount; }
    @Override
    public List<FeatureAsset> getFeatureAssets() { return Arrays.stream(featureAssets).toList(); }

    @Nonnull
    @Override
    public PropDistribution build(@Nonnull Argument argument) {
        if (super.isSkipped()) {
            return NoPropDistribution.INSTANCE;
        } else {
            SeedBox seedBox = argument.parentSeed.child(seed.build(this));
            TileSetAsset.Argument tileSetArgument = TileSetAsset.argumentFrom(argument, seedBox, this);
            PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));

            return new GridWavePropDistribution(
                    positionProvider,
                    Arrays.stream(poiTileSetAssets).flatMap(tile -> tile.build(tileSetArgument).stream()).toList(),
                    Arrays.stream(baseTileSetAssets).flatMap(tile -> tile.build(tileSetArgument).stream()).toList(),
                    Arrays.stream(fancyTileSetAssets).flatMap(tile -> tile.build(tileSetArgument).stream()).toList(),
                    tileSetArgument
            );
        }
    }
}