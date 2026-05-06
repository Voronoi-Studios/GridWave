package ch.voronoi.GridWave.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import ch.voronoi.GridWave.RuleSetNodes.RuleSet;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SingleTileSet extends TileSet {
    protected final ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps;
    protected final List<TileEntry> tileEntries;
    protected final List<FeatureAsset> tileFeatureAssets;

    public SingleTileSet(@Nonnull ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps, @Nonnull RuleSet.Combo ruleSet, double weight, boolean minimizeVariants, TileSetAsset.Argument argument, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        tileEntries = new ArrayList<>();
        this.prefabWeightedMaps = prefabWeightedMaps;
        this.tileFeatureAssets = tileFeatureAssets;
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < 4; r++) {
            RuleSet.Combo current = rotate(ruleSet,r);
            String key = Arrays.toString(current.toStringArray());
            TileEntry tileEntry = new TileEntry(Map.of(Vector3i.ZERO.clone(), current), Vector3i.ZERO.clone(), weight, r, this::getProp, new ArrayList<>(tileFeatureAssets));
            if (!minimizeVariants || seen.add(key)) tileEntries.add(tileEntry);
        }
        tileFeatureAssets.forEach(feature -> feature.AfterTileSetCreation(tileEntries, argument));
    }

    @Nonnull
    @Override
    public List<TileEntry> getTileEntries() { return tileEntries; }

    @Nonnull
    @Override
    public List<TileEntry> getAllTileEntries() { return tileEntries; }

    @Override
    public @Nonnull List<FeatureAsset> getTileFeatureAssets() { return tileFeatureAssets; }

    @Override
    public Prop getProp(@Nonnull TileSetAsset.Argument argument) {
        if(!prefabWeightedMaps.containsKey(argument.workerId.id)) return EmptyProp.INSTANCE;
        return new PrefabProp(prefabWeightedMaps.get(argument.workerId.id), argument.materialCache,argument.parentSeed);
    }
}
