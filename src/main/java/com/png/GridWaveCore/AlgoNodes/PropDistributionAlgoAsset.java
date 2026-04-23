package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.NoPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.*;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.png.GridWaveCore.AlgoNodes.Helper.DebugUtils;
import com.png.GridWaveCore.AlgoNodes.Helper.GridTile;
import com.png.GridWaveCore.AlgoNodes.Helper.MapPropDistribution;
import com.png.GridWaveCore.AlgoNodes.Helper.WaveCell;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;
import com.png.GridWaveCore.SeedNodes.PositionSeedAsset;
import com.png.GridWaveCore.SeedNodes.SeedAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import com.png.GridWaveCore.TileNodes.TileSetAsset;

import javax.annotation.Nonnull;
import java.util.*;


public class PropDistributionAlgoAsset extends PropDistributionAsset implements IAlgoAsset {
    @Nonnull
    public static final BuilderCodec<PropDistributionAlgoAsset> CODEC = BuilderCodec.builder(PropDistributionAlgoAsset.class, PropDistributionAlgoAsset::new, PropDistributionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("GridPoints", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset)
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
            .append(new KeyedCodec<>("MaxPositionsCount", Codec.INTEGER, false), (asset, v) -> asset.maxPositionsCount = v, asset -> asset.maxPositionsCount)
            .add()
            .build();

    private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
    private TileSetAsset[] poiTileSetAssets = new TileSetAsset[0];
    private TileSetAsset[] baseTileSetAssets = new TileSetAsset[0];
    private TileSetAsset[] fancyTileSetAssets = new TileSetAsset[0];
    private SeedAsset seed = new PositionSeedAsset();
    private FeatureAsset[] featureAssets = new FeatureAsset[0];

    private int maxPositionsCount = 20;

    @Override
    public void cleanUp() {
    }

    @Override
    public int getPOICount() { return poiTileSetAssets.length; }

    @Nonnull
    @Override
    public PropDistribution build(@Nonnull Argument argument) {
        int workerId = argument.workerId.id;
        if (super.isSkipped()) {
            return NoPropDistribution.INSTANCE;
        } else {
            SeedBox seedBox = argument.parentSeed.child(seed.build());

            PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
            List<Vector3d> gridPositions = GridWave.getPositions(positionProvider, maxPositionsCount);
            int grid = GridWave.getGrid(gridPositions);

            List<TileSet.TileEntry> poiTileEntries = new ArrayList<>();
            for(TileSetAsset tileSetAsset : poiTileSetAssets){
                TileSet result = tileSetAsset.build(TileSetAsset.argumentFrom(argument), grid);
                poiTileEntries.addAll(result.getAllTileEntries());
            }
            List<TileSet.TileEntry> baseTileEntries = new ArrayList<>();
            for(TileSetAsset tileSetAsset : baseTileSetAssets){
                TileSet result = tileSetAsset.build(TileSetAsset.argumentFrom(argument), grid);
                baseTileEntries.addAll(result.getAllTileEntries());
            }
            List<TileSet.TileEntry> fancyTileEntries = new ArrayList<>();
            for(TileSetAsset tileSetAsset : fancyTileSetAssets){
                TileSet result = tileSetAsset.build(TileSetAsset.argumentFrom(argument), grid);
                fancyTileEntries.addAll(result.getTileEntries());
            }

            var baseWave = GridWave.getBaseWave(poiTileEntries, baseTileEntries, gridPositions, grid, Arrays.stream(featureAssets).toList(), this);
            var wfcWave = GridWave.performWFC(baseWave, grid, seedBox, Arrays.stream(featureAssets).toList(), this, workerId);
            var fancyWave = GridWave.placeFancyTiles(wfcWave, fancyTileEntries, seedBox.child("fancy"));
            List<GridTile> gridTiles = new LinkedList<>(fancyWave.values().stream().map(WaveCell::getChosen).toList());

            if (workerId == 1) DebugUtils.sendDebugLog(gridTiles, grid,Arrays.stream(featureAssets).toList(), seedBox);

            if(gridTiles.isEmpty()) return NoPropDistribution.INSTANCE;

            Map<Vector3d, Prop> gridProps = GridWave.loadPrefabProps(TileSetAsset.argumentFrom(argument), grid, gridTiles, Arrays.stream(featureAssets).toList(), this);

            return new MapPropDistribution(gridProps);
        }
    }
}