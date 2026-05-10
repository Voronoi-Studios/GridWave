package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MultiTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps;
    protected final List<FeatureAsset> tileFeatureAssets;

    public MultiTileSet(ConcurrentHashMap<Integer, WeightedMap<List<IPrefabBuffer>>> prefabWeightedMaps, @Nonnull Map<Vector3i, RuleCombo> ruleSets, double weight, TileSetAsset.Argument argument, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        this.tileEntries = new ArrayList<>();
        this.prefabWeightedMaps = prefabWeightedMaps;
        this.tileFeatureAssets = tileFeatureAssets;
        for (int r = 0; r < 4; r++) {
            Map<Vector3i, RuleCombo> current = new HashMap<>();
            for (Map.Entry<Vector3i, RuleCombo> e : ruleSets.entrySet()) {
                Vector3i rotatedKey = rotate(e.getKey().clone(), r);
                RuleCombo rotatedValue = rotate(e.getValue(), r);
                current.put(rotatedKey, rotatedValue);
            }
            this.tileEntries.add(new TileEntry(current, Vector3i.ZERO.clone(), weight, r, this::getProp, new ArrayList<>(tileFeatureAssets)));
        }
        tileFeatureAssets.forEach(feature -> feature.AfterTileSetCreation(tileEntries, argument));
    }

    @Nonnull
    @Override
    public Stream<TileEntry> getTileEntries() { return tileEntries.stream();}

    @Nonnull
    @Override
    public Stream<TileEntry> getAllTileEntries() {
        List<TileEntry> result = new ArrayList<>();
        for(TileEntry tileEntry : tileEntries) {
            result.addAll(tileEntry.getSubTiles());
        }
        return result.stream();
    }

    @Nonnull
    @Override
    public Stream<FeatureAsset> getTileFeatureAssets() { return tileFeatureAssets.stream(); }

    @Override
    public Prop getProp(@Nonnull TileSetAsset.Argument argument) {
        if(!prefabWeightedMaps.containsKey(argument.workerId.id)) return EmptyProp.INSTANCE;
        return new PrefabProp(prefabWeightedMaps.get(argument.workerId.id), argument.materialCache,argument.parentSeed);
    }
}
