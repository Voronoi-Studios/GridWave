package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.*;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.worldgen.WorldGenPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.png.GridWaveCore.SeedNodes.RandomSeedAsset;
import com.png.GridWaveCore.SeedNodes.SeedAsset;
import com.png.GridWaveCore.TileNodes.FixedTileSet;
import com.png.GridWaveCore.TileNodes.FixedTileSetAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import com.png.GridWaveCore.TileNodes.TileSetAsset;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;


public class AlgoAsset extends PropAsset {
    @Nonnull
    public static final BuilderCodec<AlgoAsset> CODEC = BuilderCodec.builder(AlgoAsset.class, AlgoAsset::new, PropAsset.ABSTRACT_CODEC)
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
    private RuleSetAsset borderRuleSet = new RuleSetAsset();
    private SeedAsset seed = new RandomSeedAsset();

    private int maxPositionsCount = 20;
    private int maxAttempts = 500;
    private int maxBacktracks = 100;
    private String pathKey = null;
    private boolean debug = false;
    private boolean multithreading = false;

    private static final ConcurrentHashMap<String, AtomicReference<List<Algo.GridTile>>> winnerGridTilesMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> winnerMap = new ConcurrentHashMap<>();

    @Override
    public void cleanUp() {
    }

    @Nonnull
    @Override
    public Prop build(@Nonnull PropAsset.Argument argument) {
        int workerId = argument.workerId.id;
        if (super.skip()) { //I probably dont want that
            return EmptyProp.INSTANCE;
        } else {
            SeedBox seedBox = argument.parentSeed.child(seed.build());
            AtomicReference<List<Algo.GridTile>> winnerGridTiles = winnerGridTilesMap.computeIfAbsent(seedBox.toString(), k -> new AtomicReference<>());
            AtomicInteger winner = winnerMap.computeIfAbsent(seedBox.toString(), k -> new AtomicInteger(-1));

            PositionProvider positionProvider = positionProviderAsset.build(new PositionProviderAsset.Argument(argument.parentSeed, argument.referenceBundle, argument.workerId));
            List<Vector3d> gridPositions = getPositions(positionProvider, maxPositionsCount);
            int grid = getGrid(gridPositions);

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

            //start multithreading

            var baseWave = Algo.getBaseWave(poiTileEntries, baseTileEntries, gridPositions, grid, borderRuleSet.build(), this.debug);
            var wfcWave = Algo.performWFC(baseWave, grid,multithreading ? seedBox.child(workerId + "w") : seedBox, this.maxAttempts, this.maxBacktracks, this.debug, winner);
            var fancyWave = Algo.placeFancyTiles(wfcWave, fancyTileEntries,  seedBox.child("fancy"), winner);
            List<Algo.GridTile> gridTiles;
            if (multithreading) {
                if (winner.get() == -1 && winnerGridTiles.compareAndSet(null, new ArrayList<>(fancyWave.values().stream().map(Algo.WaveCell::getChosen).toList()))) {
                    winner.set(workerId);
                }
                gridTiles = new ArrayList<>(winnerGridTiles.get());
            }
            else {
                gridTiles = new ArrayList<>(fancyWave.values().stream().map(Algo.WaveCell::getChosen).toList());
            }

            if (workerId == 1) {
                String generatedString = Algo.generateString(gridTiles, pathKey);
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                final String message = "Generated "+ gridTiles.size() + " tiles based on winner worker with Id: " + winner.get() + " with grid: " + grid + " :\n" + generatedString;
                scheduler.schedule(() -> LoggerUtil.getLogger().info(message), 2, TimeUnit.SECONDS);
            }

            if(gridTiles.isEmpty()) return EmptyProp.INSTANCE;

            List<Prop> gridProps = new ArrayList<>();
            Vector3i[] anchorOffsets = getAnchorOffsets(grid);
            for (var gridTile : gridTiles) {
                if (gridTile == null) continue;
                if (gridTile.tileEntry().weightedPathAssets() == null || gridTile.tileEntry().weightedPathAssets().size() == 0) continue;
                Prop prop = new com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp(gridTile.tileEntry().weightedPathAssets(), argument.materialCache, argument.parentSeed);
                Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(gridTile.tileEntry().rotation(), Rotation.None, Rotation.None), argument.materialCache);
                //Add offsets for Multitiles
                Prop offsetProp = new OffsetProp(gridTile.positionOffset().clone().add(anchorOffsets[gridTile.tileEntry().rot()].clone()), rotatedProp);
                gridProps.add(offsetProp);
            }
            return new UnionProp(gridProps);
        }
    }

    public static int getGrid(List<Vector3d> gridPositions) {
        return (int) Math.round(gridPositions
                .stream()
                .flatMap(p1 -> gridPositions.stream().map(p2 -> p1.distanceTo(p2)))
                .filter(d -> d > 0)
                .min(Double::compare)
                .orElse(0.0)
        );
    }

    private static @NonNull Vector3i[] getAnchorOffsets(int grid) {
        int evenOffset = (grid % 2 == 0) ? 1 : 0;
        return new Vector3i[] {
                new Vector3i(0, 0, 0),
                new Vector3i(0, 0, evenOffset),
                new Vector3i(evenOffset, 0, evenOffset),
                new Vector3i(evenOffset, 0, 0)
        };
    }

    public static List<Vector3d> getPositions(PositionProvider provider, int maxPositionsCount) {
        List<Vector3d> positions = new ArrayList<>();

        Pipe.One<Vector3d> collectingPipe = (position, control) -> {
            if (positions.size() < maxPositionsCount) {
                positions.add(position.clone());
            }
        };
        PositionProvider.Context context = new PositionProvider.Context(
                new Bounds3d(
                        new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
                        new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)),
                collectingPipe,
                null
        );
        provider.generate(context);
        return positions;
    }
}