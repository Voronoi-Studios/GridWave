package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.*;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.png.GridWaveCore.AlgoNodes.WFC.DebugUtils;
import com.png.GridWaveCore.AlgoNodes.WFC.GridTile;
import com.png.GridWaveCore.AlgoNodes.WFC.WaveCell;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;
import com.png.GridWaveCore.SeedNodes.RandomSeedAsset;
import com.png.GridWaveCore.SeedNodes.SeedAsset;
import com.png.GridWaveCore.TileNodes.FixedTileSet;
import com.png.GridWaveCore.TileNodes.FixedTileSetAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import com.png.GridWaveCore.TileNodes.TileSetAsset;

import javax.annotation.Nonnull;
import java.util.*;


public class GridWavePropAsset extends PropAsset {
    @Nonnull
    public static final BuilderCodec<GridWavePropAsset> CODEC = BuilderCodec.builder(GridWavePropAsset.class, GridWavePropAsset::new, PropAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("GridPoints", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset)
            .add()
            .append(new KeyedCodec<>("POIs", new ArrayCodec<>(FixedTileSetAsset.CODEC, FixedTileSetAsset[]::new), true), (asset, v) -> asset.poiTileSetAssets = v, asset -> asset.poiTileSetAssets)
            .add()
            .append(new KeyedCodec<>("BaseTiles", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, v) -> asset.baseTileSetAssets = v, asset -> asset.baseTileSetAssets)
            .add()
            .append(new KeyedCodec<>("FancyTiles", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, v) -> asset.fancyTileSetAssets = v, asset -> asset.fancyTileSetAssets)
            .add()
            .append(new KeyedCodec<>("BorderRuleSet", RuleSetAsset.CODEC, false), (asset, v) -> asset.borderRuleSet = v, asset -> asset.borderRuleSet)
            .add()
            .append(new KeyedCodec<>("Seed", SeedAsset.CODEC, false), (asset, v) -> asset.seed = v, asset -> asset.seed)
            .add()
            .append(new KeyedCodec<>("MaxPositionsCount", Codec.INTEGER, false), (asset, v) -> asset.maxPositionsCount = v, asset -> asset.maxPositionsCount)
            .add()
            .append(new KeyedCodec<>("MaxAttempts", Codec.INTEGER, false), (asset, v) -> asset.maxAttempts = v, asset -> asset.maxAttempts)
            .add()
            .append(new KeyedCodec<>("MaxBacktracks", Codec.INTEGER, false), (asset, v) -> asset.maxBacktracks = v, asset -> asset.maxBacktracks)
            .add()
            .append(new KeyedCodec<>("PathKey", Codec.STRING, false), (asset, v) -> asset.pathKey = v, asset -> asset.pathKey)
            .add()
            .append(new KeyedCodec<>("Multithreading", Codec.BOOLEAN, false), (asset, v) -> asset.multithreading = v, asset -> asset.multithreading)
            .add()
            .append(new KeyedCodec<>("Debug", Codec.BOOLEAN, false), (asset, v) -> asset.debug = v, asset -> asset.debug)
            .add()
            .build();

    private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();
    private FixedTileSetAsset[] poiTileSetAssets = new FixedTileSetAsset[0];
    private TileSetAsset[] baseTileSetAssets = new TileSetAsset[0];
    private TileSetAsset[] fancyTileSetAssets = new TileSetAsset[0];
    private RuleSetAsset borderRuleSet = new SimpleRuleSetAsset();
    private SeedAsset seed = new RandomSeedAsset();

    private int maxPositionsCount = 20;
    private int maxAttempts = 500;
    private int maxBacktracks = 100;
    private String pathKey = null;
    private boolean debug = false;
    private boolean multithreading = false;

    @Override
    public void cleanUp() {
    }

    @Nonnull
    @Override
    public Prop build(@Nonnull PropAsset.Argument argument) {
        int workerId = argument.workerId.id;
        if (super.skip()) {
            return EmptyProp.INSTANCE;
        } else {
            SeedBox seedBox = argument.parentSeed.child(seed.build());

            PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
            List<Vector3d> gridPositions = GridWave.getPositions(positionProvider, maxPositionsCount);
            int grid = GridWave.getGrid(gridPositions);

            List<TileSet.TileEntry> poiTileEntries = new ArrayList<>();
            for(FixedTileSetAsset tileSetAsset : poiTileSetAssets){
                FixedTileSet result = tileSetAsset.build(TileSetAsset.argumentFrom(argument), grid);
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

            String[] pathKeys = pathKey.split(",");
            RuleSet pathRuleSet = new RuleSet(pathKeys,pathKeys,pathKeys,pathKeys);
            RuleSet.Combo pathRuleSetCombo = new RuleSet.Combo(pathRuleSet,pathRuleSet);

            var baseWave = GridWave.getBaseWave(poiTileEntries, baseTileEntries, gridPositions, grid, borderRuleSet.build(), pathRuleSetCombo, this.debug);
            var wfcWave = GridWave.performWFC(baseWave, grid, this.maxAttempts, this.maxBacktracks, seedBox, pathRuleSetCombo, poiTileSetAssets.length, this.multithreading, this.debug, workerId);
            var fancyWave = GridWave.placeFancyTiles(wfcWave, fancyTileEntries,seedBox.child("fancy"));
            List<GridTile> gridTiles = new ArrayList<>(fancyWave.values().stream().map(WaveCell::getChosen).toList());

            if (workerId == 1) DebugUtils.sendDebugLog(gridTiles,grid, this.pathKey, seedBox);

            if(gridTiles.isEmpty()) return EmptyProp.INSTANCE;

            Map<Vector3d, Prop> gridProps = GridWave.loadPrefabProps(GridWave.argumentFrom(argument), grid, gridTiles);
            List<Prop> offsetProps = new ArrayList<>();
            for(var entry : gridProps.entrySet()){
                offsetProps.add(new OffsetProp(entry.getKey().toVector3i(), entry.getValue()));
            }
            return new UnionProp(offsetProps);
        }
    }
}