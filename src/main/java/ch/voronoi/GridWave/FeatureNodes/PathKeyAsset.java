package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.DebugUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;
import ch.voronoi.GridWave.RuleSetNodes.RuleSet;
import com.hypixel.hytale.server.core.Message;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ch.voronoi.GridWave.AlgoNodes.Helper.Match.oppositeDirection;

public class PathKeyAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<PathKeyAsset> CODEC = BuilderCodec.builder(
                    PathKeyAsset.class, PathKeyAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("PathKey", Codec.STRING, true), (asset, value) -> asset.pathKey = value, asset -> asset.pathKey)
            .add()
            .append(new KeyedCodec<>("CleanIsolated", Codec.BOOLEAN, true), (asset, value) -> asset.cleanIsolated = value, asset -> asset.cleanIsolated)
            .add()
            .build();

    private String pathKey = "";
    private boolean cleanIsolated;

    @Override
    public void AfterNeighbourPropagation(WaveCell source, int rot, WaveCell neighbor, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        if(super.skip()) return;
        if(neighbor.isCollapsed()){
            String[][] neighborRuleSetArrays = neighbor.getChosen().tileEntry().getMainRuleSet().providerRuleSet().getRuleSetArrays();
            if(Arrays.stream(neighborRuleSetArrays[oppositeDirection[rot]]).toList().contains(pathKey)) {
                source.connectedPOIs.addAll(neighbor.connectedPOIs);
            }
        }
    }

    @Override
    public boolean FinalCheck(Map<Vector3i, WaveCell> baseWave, int workerId, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        if(skip()) return true;
        final int count = algoAsset.getPOICount();
        boolean check = baseWave.values().stream().anyMatch(x -> x.connectedPOIs.size() >= count);
        if(check && cleanIsolated) baseWave.entrySet().removeIf(x -> x.getValue().connectedPOIs.size() < algoAsset.getPOICount());
        return check;
    }

    public String[] getPathKeys() { return pathKey.split(","); }
}
