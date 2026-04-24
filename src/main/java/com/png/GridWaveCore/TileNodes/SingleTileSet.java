package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;

import javax.annotation.Nonnull;
import java.util.*;

public class SingleTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final WeightedMap<List<IPrefabBuffer>> prefabWeightedMap;
    protected final List<FeatureAsset> tileFeatureAssets;

    public SingleTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull RuleSet.Combo ruleSet, double weight, boolean minimizeVariants, TileSetAsset.Argument argument, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        tileEntries = new ArrayList<>();
        this.prefabWeightedMap = prefabWeightedMap;
        this.tileFeatureAssets = tileFeatureAssets;
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < 4; r++) {
            RuleSet.Combo current = rotate(ruleSet,r);
            String key = Arrays.toString(current.getDebug());
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
    public Prop getProp(TileSetAsset.Argument argument) {
        return new PrefabProp(prefabWeightedMap, argument.materialCache,argument.parentSeed);
    }
}
