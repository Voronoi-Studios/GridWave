package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.*;
import ch.voronoi.GridWave.SeedNodes.ConstantSeedAsset;
import ch.voronoi.GridWave.TileCollectionNodes.SimpleTileSetCollectionAsset;
import ch.voronoi.GridWave.TileCollectionNodes.TileSetCollectionAsset;
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
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.*;


public class PropDistributionAlgoAsset extends PropDistributionAsset implements IAlgoAsset {
    @Nonnull
    public static final BuilderCodec<PropDistributionAlgoAsset> CODEC = BuilderCodec.builder(PropDistributionAlgoAsset.class, PropDistributionAlgoAsset::new, PropDistributionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("GridPoints", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset)
            .add()
            .append(new KeyedCodec<>("Grid", Vector3i.CODEC, true), (asset, v) -> asset.grid = v, asset -> asset.grid)
            .add()
            .append(new KeyedCodec<>("POIs", TileSetCollectionAsset.CODEC, true), (asset, v) -> asset.poiTileSetAssets = v, asset -> asset.poiTileSetAssets)
            //.addValidator(new POIValidator())
            .add()
            .append(new KeyedCodec<>("BaseTiles", TileSetCollectionAsset.CODEC, true), (asset, v) -> asset.baseTileSetAssets = v, asset -> asset.baseTileSetAssets)
            .add()
            .append(new KeyedCodec<>("FancyTiles", TileSetCollectionAsset.CODEC, true), (asset, v) -> asset.fancyTileSetAssets = v, asset -> asset.fancyTileSetAssets)
            .add()
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("Features", new ArrayCodec<>(FeatureAsset.CODEC, FeatureAsset[]::new), true), (asset, v) -> asset.featureAssets = v, asset -> asset.featureAssets)
            .add()
            .append(new KeyedCodec<>("MaxPositionsCount", Codec.INTEGER, false), (asset, v) -> asset.maxPositionsCount = v, asset -> asset.maxPositionsCount)
            .add()
            .build();

    private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
    private TileSetCollectionAsset poiTileSetAssets = new SimpleTileSetCollectionAsset();
    private TileSetCollectionAsset baseTileSetAssets = new SimpleTileSetCollectionAsset();
    private TileSetCollectionAsset fancyTileSetAssets = new SimpleTileSetCollectionAsset();
    private SeedAsset seed = new ConstantSeedAsset();
    private FeatureAsset[] featureAssets = new FeatureAsset[0];

    private Vector3i grid = new Vector3i(16,16,16);
    private int maxPositionsCount = 20;

    @Override
    public void cleanUp() { }
    @Override
    public int getPOICount() { return poiTileSetAssets.getTileSetAssets().length; }
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
                    poiTileSetAssets.build(tileSetArgument),
                    baseTileSetAssets.build(tileSetArgument),
                    fancyTileSetAssets.build(tileSetArgument),
                    tileSetArgument
            );
        }
    }
}