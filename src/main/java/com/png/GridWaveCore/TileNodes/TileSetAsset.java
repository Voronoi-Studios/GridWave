package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.PrefabLoader;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TileSetAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, TileSetAsset>>, Cleanable {
    @Nonnull
    public static final AssetCodecMapCodec<String, TileSetAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );
    @Nonnull
    private static final Map<String, TileSetAsset.Exported> exportedNodes = new ConcurrentHashMap<>();
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(TileSetAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<TileSetAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(TileSetAsset.class)
            .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
            .add()
            .afterDecode(asset -> {
                if (asset.exportName != null && !asset.exportName.isEmpty()) {
                    if (exportedNodes.containsKey(asset.exportName)) {
                        LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
                    }

                    TileSetAsset.Exported exported = new TileSetAsset.Exported();
                    exported.asset = asset;
                    exportedNodes.put(asset.exportName, exported);
                    LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
                }
            })
            .append(new KeyedCodec<>("Features", new ArrayCodec<>(FeatureAsset.CODEC, FeatureAsset[]::new), false),
                    (asset, v) -> asset.tileFeatureAssets = v,
                    asset -> asset.tileFeatureAssets)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private String exportName = "";
    private @Nonnull FeatureAsset[] tileFeatureAssets = new FeatureAsset[0];
    private boolean legacyPath = false;

    public abstract TileSet build(@Nonnull TileSetAsset.Argument argument, int grid);

    @Override
    public void cleanUp() {
    }

    public static TileSetAsset.Exported getExportedAsset(@Nonnull String name) {
        return exportedNodes.get(name);
    }

    public String getId() {
        return this.id;
    }

    public @Nonnull List<FeatureAsset> getTileFeatureAssets() {return new ArrayList<>(List.of(this.tileFeatureAssets)); }

    @Nonnull
    public static TileSetAsset.Argument argumentFrom(@Nonnull PropAsset.Argument argument, IAlgoAsset algoAsset) {
        return new TileSetAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId, algoAsset);
    }
    @Nonnull
    public static TileSetAsset.Argument argumentFrom(@Nonnull PropDistributionAsset.Argument argument, IAlgoAsset algoAsset) {
        return new TileSetAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId, algoAsset);
    }
    @Nonnull
    public static PropAsset.Argument argumentFrom(@Nonnull TileSetAsset.Argument argument) {
        return new PropAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId);
    }

    public static class Exported {
        public TileSetAsset asset;
    }

    public static class Argument {
        public SeedBox parentSeed;
        public MaterialCache materialCache;
        public ReferenceBundle referenceBundle;
        public WorkerIndexer.Id workerId;
        public IAlgoAsset algoAsset;

        public Argument(
                @Nonnull SeedBox parentSeed,
                @Nonnull MaterialCache materialCache,
                @Nonnull ReferenceBundle referenceBundle,
                @Nonnull WorkerIndexer.Id workerId,
                @Nonnull IAlgoAsset algoAsset
        ) {
            this.parentSeed = parentSeed;
            this.materialCache = materialCache;
            this.referenceBundle = referenceBundle;
            this.workerId = workerId;
            this.algoAsset = algoAsset;
        }

        public Argument(@Nonnull TileSetAsset.Argument argument) {
            this.parentSeed = argument.parentSeed;
            this.materialCache = argument.materialCache;
            this.referenceBundle = argument.referenceBundle;
            this.workerId = argument.workerId;
            this.algoAsset = argument.algoAsset;
        }
    }

    @Nullable
    public List<IPrefabBuffer> loadPrefabBuffersFrom(@Nonnull String path) {
        List<IPrefabBuffer> loadedPrefabs = new LinkedList<>();
        Set<Path> traversedPaths = new LinkedHashSet<>();
        List<AssetPack> packs = AssetModule.get().getAssetPacks();

        for (int i = packs.size() - 1; i >= 0; i--) {
            Path packRootPath = packs.get(i).getRoot();
            Path prefabsDir = packRootPath.resolve("Server");
            if (this.legacyPath) {
                prefabsDir = prefabsDir.resolve("World").resolve("Default").resolve("Prefabs");
            } else {
                prefabsDir = prefabsDir.resolve("Prefabs");
            }

            Path fullPath = PathUtil.resolvePathWithinDir(prefabsDir, path);
            if (fullPath != null) {
                try {
                    PrefabLoader.traverseAllPrefabBuffersUnder(fullPath, (fullPrefabPath, prefab) -> {
                        Path relativePrefabPath = fullPrefabPath.subpath(packRootPath.getNameCount(), fullPrefabPath.getNameCount());
                        if (!traversedPaths.contains(relativePrefabPath)) {
                            traversedPaths.add(relativePrefabPath);
                            loadedPrefabs.add(prefab);
                        }
                    });
                } catch (Exception var11) {
                    String msg = "Couldn't load prefab with path: " + path;
                    msg = msg + "\n";
                    msg = msg + ExceptionUtil.toStringWithStack(var11);
                    LoggerUtil.getLogger().severe(msg);
                    return null;
                }
            }
        }

        return loadedPrefabs;
    }
}
