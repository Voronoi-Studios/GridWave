package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;

import javax.annotation.Nonnull;
import java.util.*;

public class SingleTileSet extends TileSet {
    private final List<TileEntry> tileEntries;

    public SingleTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull String[] ruleSet, double weight, boolean autoRot) {
        tileEntries = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < (autoRot ? 4 : 1); r++) {
            String[] current = rotate(ruleSet.clone(),r);
            String key = Arrays.toString(current);
            if (seen.add(key)) tileEntries.add(new TileEntry(Map.of(Vector3i.ZERO.clone(), current), Vector3i.ZERO.clone(), weight, r,prefabWeightedMap));
        }
    }

    @Nonnull
    @Override
    public List<TileEntry> getTileEntries() { return tileEntries; }

    @Nonnull
    @Override
    public List<TileEntry> getAllTileEntries() { return tileEntries; }
}
