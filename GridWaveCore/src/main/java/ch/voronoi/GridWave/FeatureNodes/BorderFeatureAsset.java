package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;
import ch.voronoi.GridWave.TileSetNodes.SingleTileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ch.voronoi.GridWave.AlgoNodes.GridWave.dirs;

public class BorderFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<BorderFeatureAsset> CODEC = BuilderCodec.builder(
                    BorderFeatureAsset.class, BorderFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("BorderRuleSet", RuleSetAsset.CODEC, false), (asset, v) -> asset.borderRuleSet = v, asset -> asset.borderRuleSet)
            .add()
            .build();

    private RuleSetAsset borderRuleSet = new SimpleRuleSetAsset();


    @Override
    public void BaseWaveProcessor(@NonNull List<Vector3d> gridPositions, Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) {
        if(skip()) return;
        Set<Vector3i> borderPositions = new LinkedHashSet<>();
        for (Vector3d pos3d : gridPositions) {
            for (int d = 0; d < 4; d++) {
                Vector3i neighbor = new Vector3i(pos3d.toVector3i()).add(dirs[d].clone().scale(argument.algoAsset.getGrid()));
                if (!baseWave.containsKey(neighbor)) borderPositions.add(neighbor); //If its an outer tile
            }
        }

        TileSet.TileEntry borderTile = new SingleTileSet(new ConcurrentHashMap<>(), borderRuleSet.build(),1,false, argument, new ArrayList<>()).getTileEntries().toList().getFirst();
        for(Vector3i borderPos : borderPositions){
            WaveCell waveCell = new WaveCell(borderPos.clone(),borderTile, GridTileType.BASIC);
            GridWave.propagate(waveCell, baseWave, null, argument);
        }
    }
}
