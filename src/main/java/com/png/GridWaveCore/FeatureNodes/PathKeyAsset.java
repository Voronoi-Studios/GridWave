package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.joml.Vector3i;import com.png.GridWaveCore.AlgoNodes.Helper.Match;
import com.png.GridWaveCore.AlgoNodes.Helper.WaveCell;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class PathKeyAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PathKeyAsset> CODEC = BuilderCodec.builder(
                    PathKeyAsset.class, PathKeyAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("PathKey", Codec.STRING, true), (asset, value) -> asset.pathKey = value, asset -> asset.pathKey)
            .add()
            .build();

    private String pathKey = "";

    private RuleSet.Combo combo = null;
    private RuleSet.Combo build(){
        if (combo == null){
            String[] pathKeys = pathKey.split(",");
            RuleSet pathRuleSet = new RuleSet(pathKeys,pathKeys,pathKeys,pathKeys);
            combo = new RuleSet.Combo(pathRuleSet, pathRuleSet);
        }
        return combo;
    }

    @Override
    public void AfterNeighbourPropagation(WaveCell source, int rot, WaveCell neighbor, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        if(super.skip()) return;
        if(neighbor.isCollapsed() && Match.dir(rot, neighbor.getChosen().tileEntry().getMainRuleSet(),build())) {
            source.connectedPOIs.addAll(neighbor.connectedPOIs);
        }
    }

    @Override
    public boolean FinalCheck(Map<Vector3ic, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        return baseWave.values().stream().anyMatch(x -> x.connectedPOIs.size() >= algoAsset.getPOICount());
    }

    public String[] getPathKeys() { return pathKey.split(","); }
}
