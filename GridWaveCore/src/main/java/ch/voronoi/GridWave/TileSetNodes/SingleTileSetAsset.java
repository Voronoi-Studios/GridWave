package ch.voronoi.GridWave.TileSetNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SingleTileSetAsset extends TileSetAsset {

    @Nonnull
    public static final BuilderCodec<SingleTileSetAsset> CODEC = BuilderCodec.builder(SingleTileSetAsset.class, SingleTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSetAsset.CODEC, true), (asset, value) -> asset.ruleSetAsset = value, asset -> asset.ruleSetAsset)
            .add()
            .append(new KeyedCodec<>("PrefabPath", Codec.STRING, true), (asset, v) -> asset.prefabPath = v, asset -> asset.prefabPath)
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .append(new KeyedCodec<>("MinimizeVariants", Codec.BOOLEAN, true), (asset, value) -> asset.minimizeVariants = value, asset -> asset.minimizeVariants)
            .add()
            .build();
    private RuleSetAsset ruleSetAsset = new SimpleRuleSetAsset();
    private String prefabPath = "";
    private double weight = 1;
    private boolean minimizeVariants = true;

    public static final ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>>> prefabBufferCache = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull TileSetAsset.Argument argument) {
        var prefabWeightedMaps = prefabBufferCache.computeIfAbsent(prefabPath, k -> new ConcurrentHashMap<>());
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        if(!prefabPath.isEmpty()) {
            List<IPrefabBuffer> pathPrefabs = TileSetAsset.loadPrefabBuffersFrom(prefabPath, false);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
                prefabWeightedMap.add(pathPrefabs, 1);
            }
        }
        prefabWeightedMaps.put(argument.workerId.id, prefabWeightedMap);
        return new ArrayList<>(List.of((new SingleTileSet(prefabWeightedMaps,ruleSetAsset.build(), weight, minimizeVariants, argument, super.getTileFeatureAssets())))) ;
    }
}
