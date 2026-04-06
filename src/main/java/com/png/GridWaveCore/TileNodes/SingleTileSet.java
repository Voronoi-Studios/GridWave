package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.util.*;

public class SingleTileSet extends TileSet {
    private final List<TileEntry> tileEntries;

    public SingleTileSet(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabWeightedMap, @Nonnull RuleSet.Combo ruleSet, double weight, boolean autoRot) {
        tileEntries = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < (autoRot ? 4 : 1); r++) {
            RuleSet.Combo current = rotate(ruleSet,r);
            String key = Arrays.toString(current.getDebug());
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
