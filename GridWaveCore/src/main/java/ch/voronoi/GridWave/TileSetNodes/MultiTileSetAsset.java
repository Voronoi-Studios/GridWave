package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MultiTileSetAsset extends TileSetAsset {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>>> prefabBufferCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps;

    @Nonnull
    public static final BuilderCodec<MultiTileSetAsset> CODEC = BuilderCodec.builder(MultiTileSetAsset.class, MultiTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSets", new ArrayCodec<>(RuleSetAsset.CODEC, RuleSetAsset[]::new), true), (asset, value) -> asset.ruleSetAssets = value, asset -> asset.ruleSetAssets)
            .add()
            .append(new KeyedCodec<>("PrefabPath", Codec.STRING, true),(asset, v) -> asset.prefabPath = v,asset -> asset.prefabPath)
            .add()
            .append(new KeyedCodec<>("SizeX", Codec.INTEGER, true), (asset, value) -> asset.xSize = value, asset -> asset.xSize)
            .add()
            .append(new KeyedCodec<>("SizeZ", Codec.INTEGER, true), (asset, value) -> asset.zSize = value, asset -> asset.zSize)
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .build();
    private RuleSetAsset[] ruleSetAssets = new SimpleRuleSetAsset[0];
    private String prefabPath = "";
    private int xSize;
    private int zSize;
    private double weight = 1;

    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument, FeatureAsset... addFeatures) {
        this.prefabWeightedMaps = prefabBufferCache.computeIfAbsent(prefabPath, k -> new ConcurrentHashMap<>());
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        if(!prefabPath.isEmpty()) {
            List<IPrefabBuffer> pathPrefabs = TileSetAsset.loadPrefabBuffersFrom(prefabPath);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
                prefabWeightedMap.add(pathPrefabs, 1);
            }
        }
        prefabWeightedMaps.put(argument.workerId.id, prefabWeightedMap);

        Vector3ic size = new Vector3i(xSize, 0 ,zSize);
        Map<Vector3ic, RuleCombo> ruleSets = getRuleComboMap(size, ruleSetAssets, argument);

        return new ArrayList<>(List.of(new TileSet(ruleSets, weight, super.minimizeVariants, this::buildProp, argument, Stream.concat(Arrays.stream(this.tileFeatureAssets), Arrays.stream(addFeatures)).toList())));
    }

    private Prop buildProp(@Nonnull TileSetAsset.Argument argument) {
        Prop prop = EmptyProp.INSTANCE;
        if(prefabWeightedMaps.containsKey(argument.workerId.id)) {
            prop = new PrefabProp(prefabWeightedMaps.get(argument.workerId.id), argument.materialCache,argument.parentSeed,TileSetAsset::loadPrefabBuffersFrom);
        }
        return prop;
    }
}
