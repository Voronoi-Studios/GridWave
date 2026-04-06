package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.util.*;

public class AutoTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<AutoTileSetAsset> CODEC = BuilderCodec.builder(AutoTileSetAsset.class, AutoTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("FolderPath", Codec.STRING, true), (t, k) -> t.folderPath = k, k -> k.folderPath)
            .documentation("Example: `Maze/FancyTiles/1x2/10X0-X010`")
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .append(new KeyedCodec<>("AutoRot", Codec.BOOLEAN, true), (asset, value) -> asset.autoRot = value, asset -> asset.autoRot)
            .add()
            .build();
    private String folderPath;
    private double weight = 1;
    private boolean autoRot = true;

    @Nonnull
    @Override
    public MultiTileSet build(@Nonnull TileSetAsset.Argument argument, int grid) {
        WeightedMap<List<IPrefabBuffer>> prefabWeightedMap = new WeightedMap<>();
        List<IPrefabBuffer> pathPrefabs = this.loadPrefabBuffersFrom(folderPath);
        if (pathPrefabs != null && !pathPrefabs.isEmpty()) prefabWeightedMap.add(pathPrefabs, 1);

        List<String> parts = Arrays.stream(folderPath.split("/")).toList();

        int zSize = Integer.parseInt(parts.get(parts.size() - 2).split("x")[1]);

        String folderName = parts.getLast();
        List<String> tiles = Arrays.stream(folderName.split("-")).toList();
        List<RuleSet> ruleSetAssets = tiles.stream().map(t -> new RuleSet(t.split(""))).toList();

        Map<Vector3i, RuleSet.Combo> ruleSets = new HashMap<>();
        Vector3i offset = Vector3i.ZERO.clone();
        for(RuleSet ruleSetAsset : ruleSetAssets){
            ruleSets.put(offset.clone().scale(grid),new RuleSet.Combo(ruleSetAsset, ruleSetAsset));
            offset.z++;
            if(offset.z >= zSize) {
                offset.z = 0;
                offset.x++;
            }
        }

        return new MultiTileSet(prefabWeightedMap, ruleSets, weight, autoRot);
    }
}
