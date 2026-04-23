package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.FeatureNodes.PositionRestrictionAsset;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;

import javax.annotation.Nonnull;
import java.util.*;

public class MultiTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final WeightedMap<List<IPrefabBuffer>> prefabWeightedMap;
    protected final List<FeatureAsset> tileFeatureAssets;

    public MultiTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull Map<Vector3i, RuleSet.Combo> ruleSets, double weight, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        this.tileEntries = new ArrayList<>();
        this.prefabWeightedMap = prefabWeightedMap;
        this.tileFeatureAssets = tileFeatureAssets;
        for (int r = 0; r < 4; r++) {
            Map<Vector3i, RuleSet.Combo> current = new HashMap<>();
            for (Map.Entry<Vector3i, RuleSet.Combo> e : ruleSets.entrySet()) {
                Vector3i rotatedKey = rotate(e.getKey().clone(), r);
                RuleSet.Combo rotatedValue = rotate(e.getValue(), r);
                current.put(rotatedKey, rotatedValue);
            }
            this.tileEntries.add(new TileEntry(current, Vector3i.ZERO.clone(), weight, r, this::getProp, new ArrayList<>(tileFeatureAssets)));
        }
        tileFeatureAssets.forEach(feature -> feature.AfterTileSetCreation(tileEntries));
    }

    @Nonnull
    @Override
    public List<TileEntry> getTileEntries() { return tileEntries;}

    @Nonnull
    @Override
    public List<TileEntry> getAllTileEntries() {
        List<TileEntry> result = new ArrayList<>();
        for(TileEntry tileEntry : tileEntries) {
            result.addAll(tileEntry.getSubTiles());
        }
        return result;
    }

    @Nonnull
    @Override
    public List<FeatureAsset> getTileFeatureAssets() { return tileFeatureAssets; }

    @Override
    public Prop getProp(TileSetAsset.Argument argument) {
        return new PrefabProp(prefabWeightedMap, argument.materialCache,argument.parentSeed);
    }
}
