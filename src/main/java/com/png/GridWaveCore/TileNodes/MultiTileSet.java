package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.util.*;

public class MultiTileSet extends TileSet {
    protected List<TileEntry> tileEntries;

    public MultiTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull Map<Vector3i, RuleSet.Combo> ruleSets, double weight, boolean autoRot) {
        this.tileEntries = new ArrayList<>();
        for (int r = 0; r < (autoRot ? 4 : 1); r++) {
            Map<Vector3i, RuleSet.Combo> current = new HashMap<>();
            for (Map.Entry<Vector3i, RuleSet.Combo> e : ruleSets.entrySet()) {
                Vector3i rotatedKey = rotate(e.getKey().clone(), r);
                RuleSet.Combo rotatedValue = rotate(e.getValue(), r);
                current.put(rotatedKey, rotatedValue);
            }
            this.tileEntries.add(new TileEntry(current, Vector3i.ZERO.clone(), weight, r, prefabWeightedMap));
        }
    }
    public MultiTileSet(List<TileEntry> tileEntries) { this.tileEntries = tileEntries; }

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
}
