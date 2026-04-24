package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.*;

public class AutoTileSetAsset extends TileSetAsset {
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

    @Nonnull
    @Override
    public MultiTileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        if(!folderPath.isEmpty()) {
            List<IPrefabBuffer> pathPrefabs = loadPrefabBuffersFrom(folderPath);
            if (pathPrefabs != null && !pathPrefabs.isEmpty()) prefabWeightedMap.add(pathPrefabs, 1);
        }

        List<String> parts = Arrays.stream(folderPath.split("/")).toList();

        int zSize = Integer.parseInt(parts.get(parts.size() - 2).split("x")[1]);

        String folderName = parts.getLast();
        List<String> tiles = Arrays.stream(folderName.split("-")).toList();
        List<RuleSet> ruleSetAssets = tiles.stream().map(t -> RuleSet.createSimpleFrom(t.split(""))).toList();

        Map<Vector3ic, RuleSet.Combo> ruleSets = new HashMap<>();
        Vector3i offset = new Vector3i(Vector3iUtil.ZERO);
        for(RuleSet ruleSetAsset : ruleSetAssets){
            ruleSets.put(new Vector3i(offset).mul(grid),new RuleSet.Combo(ruleSetAsset, ruleSetAsset));
            offset.z++;
            if(offset.z >= zSize) {
                offset.z = 0;
                offset.x++;
            }
        }

        return new MultiTileSet(prefabWeightedMap, ruleSets, weight, super.getTileFeatureAssets());
    }
}
