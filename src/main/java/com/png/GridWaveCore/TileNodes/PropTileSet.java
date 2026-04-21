package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.PrefabProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.TileFeatures.PositionRestrictionAsset;
import com.png.GridWaveCore.TileFeatures.TileFeatureAsset;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final PropAsset propAsset;
    protected final List<TileFeatureAsset> tileFeatureAssets;

    public PropTileSet(PropAsset propAsset, @Nonnull Map<Vector3i, RuleSet.Combo> ruleSets, double weight, @Nonnull List<TileFeatureAsset> tileFeatureAssets) {
        this.tileEntries = new ArrayList<>();
        this.propAsset = propAsset;
        this.tileFeatureAssets = tileFeatureAssets;
        for (int r = 0; r < 4; r++) {
            final Rotation rot = Rotation.ofDegrees(r * 90);
            if (tileFeatureAssets.stream().anyMatch(a -> a instanceof PositionRestrictionAsset p && p.rot != rot)) continue;
            Map<Vector3i, RuleSet.Combo> current = new HashMap<>();
            for (Map.Entry<Vector3i, RuleSet.Combo> e : ruleSets.entrySet()) {
                Vector3i rotatedKey = rotate(e.getKey().clone(), r);
                RuleSet.Combo rotatedValue = rotate(e.getValue(), r);
                current.put(rotatedKey, rotatedValue);
            }
            TileEntry tileEntry = new TileEntry(current, Vector3i.ZERO.clone(), weight, r, this::getProp, new ArrayList<>(tileFeatureAssets));
            tileFeatureAssets.stream().filter(a -> a instanceof PositionRestrictionAsset).findFirst()
                    .ifPresent(asset -> offsetTileEntry(tileEntry, ((PositionRestrictionAsset)asset).pos));
            this.tileEntries.add(tileEntry);}
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
    public List<TileFeatureAsset> getTileFeatureAssets() { return tileFeatureAssets; }

    @Override
    public Prop getProp(TileSetAsset.Argument argument) { return propAsset.build(TileSetAsset.argumentFrom(argument)); }
}
