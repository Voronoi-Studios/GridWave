package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.FeatureNodes.PositionRestrictionAsset;
import com.png.GridWaveCore.FeatureNodes.FeatureAsset;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropTileSet extends TileSet {
    protected final List<TileEntry> tileEntries;
    protected final PropAsset propAsset;
    protected final List<FeatureAsset> tileFeatureAssets;

    public PropTileSet(PropAsset propAsset, @Nonnull Map<Vector3ic, RuleSet.Combo> ruleSets, double weight, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        this.tileEntries = new ArrayList<>();
        this.propAsset = propAsset;
        this.tileFeatureAssets = tileFeatureAssets;
        for (int r = 0; r < 4; r++) {
            Map<Vector3ic, RuleSet.Combo> current = new HashMap<>();
            for (Map.Entry<Vector3ic, RuleSet.Combo> e : ruleSets.entrySet()) {
                Vector3ic rotatedKey = rotate(e.getKey(), r);
                RuleSet.Combo rotatedValue = rotate(e.getValue(), r);
                current.put(rotatedKey, rotatedValue);
            }
            this.tileEntries.add(new TileEntry(current, Vector3iUtil.ZERO, weight, r, this::getProp, new ArrayList<>(tileFeatureAssets)));
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
    public Prop getProp(TileSetAsset.Argument argument) { return propAsset.build(TileSetAsset.argumentFrom(argument)); }
}
