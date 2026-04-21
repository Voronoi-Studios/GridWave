package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.TileFeatures.PositionRestrictionAsset;
import com.png.GridWaveCore.TileFeatures.TileFeatureAsset;

import javax.annotation.Nonnull;
import java.util.*;

public class SingleTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final WeightedMap<List<IPrefabBuffer>> prefabWeightedMap;
    protected final List<TileFeatureAsset> tileFeatureAssets;

    public SingleTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull RuleSet.Combo ruleSet, double weight, boolean minimizeVariants, @Nonnull List<TileFeatureAsset> tileFeatureAssets) {
        tileEntries = new ArrayList<>();
        this.prefabWeightedMap = prefabWeightedMap;
        this.tileFeatureAssets = tileFeatureAssets;
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < 4; r++) {
            final Rotation rot = Rotation.ofDegrees(r * 90);
            if (tileFeatureAssets.stream().anyMatch(a -> a instanceof PositionRestrictionAsset p && p.rot != rot)) continue;
            RuleSet.Combo current = rotate(ruleSet,r);
            String key = Arrays.toString(current.getDebug());
            TileEntry tileEntry = new TileEntry(Map.of(Vector3i.ZERO.clone(), current), Vector3i.ZERO.clone(), weight, r, this::getProp, new ArrayList<>(tileFeatureAssets));
            tileFeatureAssets.stream().filter(a -> a instanceof PositionRestrictionAsset).findFirst()
                    .ifPresent(asset -> offsetTileEntry(tileEntry, ((PositionRestrictionAsset)asset).pos));
            if (!minimizeVariants || seen.add(key)) tileEntries.add(tileEntry);
        }
    }

    @Nonnull
    @Override
    public List<TileEntry> getTileEntries() { return tileEntries; }

    @Nonnull
    @Override
    public List<TileEntry> getAllTileEntries() { return tileEntries; }

    @Override
    public @Nonnull List<TileFeatureAsset> getTileFeatureAssets() { return tileFeatureAssets; }

    @Override
    public Prop getProp(TileSetAsset.Argument argument) {
        return new PrefabProp(prefabWeightedMap, argument.materialCache,argument.parentSeed);
    }
}
