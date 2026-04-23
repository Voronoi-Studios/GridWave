package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.AlgoNodes.GridWave;
import com.png.GridWaveCore.AlgoNodes.Helper.GridTileType;
import com.png.GridWaveCore.AlgoNodes.Helper.WaveCell;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;
import com.png.GridWaveCore.TileNodes.SingleTileSet;
import com.png.GridWaveCore.TileNodes.TileSet;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

import static com.png.GridWaveCore.AlgoNodes.GridWave.dirs;

public class BorderAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<BorderAsset> CODEC = BuilderCodec.builder(
                    BorderAsset.class, BorderAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("BorderRuleSet", RuleSetAsset.CODEC, false), (asset, v) -> asset.borderRuleSet = v, asset -> asset.borderRuleSet)
            .add()
            .build();

    private RuleSetAsset borderRuleSet = new SimpleRuleSetAsset();


    @Override
    public void BaseWaveProcessor(@NonNull List<Vector3d> gridPositions, int grid, Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) {
        Set<Vector3i> borderPositions = new LinkedHashSet<>();
        for (Vector3d pos3d : gridPositions) {
            for (int d = 0; d < 4; d++) {
                Vector3i neighbor = new Vector3i(pos3d.toVector3i()).add(dirs[d].clone().scale(grid));
                if (!baseWave.containsKey(neighbor)) borderPositions.add(neighbor); //If its an outer tile
            }
        }

        TileSet.TileEntry borderTile = new SingleTileSet(new WeightedMap<>(), borderRuleSet.build(),1,false, new ArrayList<>(0)).getTileEntries().getFirst();
        for(Vector3i borderPos : borderPositions){
            WaveCell waveCell = new WaveCell(borderPos.clone(),borderTile, GridTileType.BASIC);
            GridWave.propagate(waveCell, baseWave, null, grid, featureAssets, algoAsset);
        }
    }
}
