package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.HorizontalRules;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleSet;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class AutoTileSetAsset extends TileSetAsset {
    public static final ConcurrentHashMap<String, ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>>> prefabBufferCache = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps;

    @Nonnull
    public static final BuilderCodec<AutoTileSetAsset> CODEC = BuilderCodec.builder(AutoTileSetAsset.class, AutoTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("FolderPath", Codec.STRING, true), (t, k) -> t.folderPath = k, k -> k.folderPath)
            .documentation("Uses the folder naming to create the ruleset for the MultiTiles\nExample: `Maze/FancyTiles/1x2/10X0-X010`")
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .build();
    private String folderPath;
    private double weight = 1;

    public AutoTileSetAsset(){
    }

    public AutoTileSetAsset(String folderPath, FeatureAsset[] featureAssets){
        this.folderPath = folderPath;
        super.tileFeatureAssets = featureAssets;
    }
    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument, FeatureAsset... addFeatures) {
        this.prefabWeightedMaps = prefabBufferCache.computeIfAbsent(folderPath, k -> new ConcurrentHashMap<>());
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        if(!folderPath.isEmpty()) {
            List<IPrefabBuffer> pathPrefabs = TileSetAsset.loadPrefabBuffersFrom(folderPath);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) {
                prefabWeightedMap.add(pathPrefabs, 1);
            }
        }
        prefabWeightedMaps.put(argument.workerId.id, prefabWeightedMap);

        List<String> parts = Arrays.stream(Path.of(folderPath).toString().split("\\\\")).toList();
        Vector3ic size = getSize(parts.get(parts.size()-2));
        if(size == null) return new ArrayList<>();
        RuleCombo[] simpleRuleSets = buildRuleCombo(parts.getLast(),null);
        Map<Vector3ic, RuleCombo> ruleSets = getRuleComboMap(argument.algoAsset.getGrid(), size, simpleRuleSets);

        return new LinkedList<>(List.of(new TileSet(ruleSets, weight, super.minimizeVariants, this::buildProp, argument,Stream.concat(Arrays.stream(this.tileFeatureAssets), Arrays.stream(addFeatures)).toList())));
    }

    private Prop buildProp(@Nonnull TileSetAsset.Argument argument) {
        Prop prop = EmptyProp.INSTANCE;
        if(prefabWeightedMaps.containsKey(argument.workerId.id)) {
            prop = new PrefabProp(prefabWeightedMaps.get(argument.workerId.id), argument.materialCache,argument.parentSeed,TileSetAsset::loadPrefabBuffersFrom);
        }
        return prop;
    }
}
