package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.BorderType;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.MirrorNode.Helper.MirrorDirection;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
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
import static ch.voronoi.GridWave.AlgoNodes.GridWave.toCellPos;

public class BorderFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<BorderFeatureAsset> CODEC = BuilderCodec.builder(
                    BorderFeatureAsset.class, BorderFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("BorderType", BorderType.CODEC, true), (asset, v) -> asset.borderType = v, asset -> asset.borderType)
            .add()
            .append(new KeyedCodec<>("BorderRuleSets", new ArrayCodec<>(RuleSetAsset.CODEC, RuleSetAsset[]::new), false), (asset, v) -> asset.borderRuleSets = v, asset -> asset.borderRuleSets)
            .add()
            .build();

    private BorderType borderType = BorderType.OuterBorder;
    private RuleSetAsset[] borderRuleSets = new RuleSetAsset[0];


    @Override
    public void BaseWaveProcessor(@NonNull Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) {
        if(skip() || borderRuleSets.length == 0) return;
        Bounds3i fullBounds = argument.algoAsset.getFullBounds().clone();
        Vector3i grid = argument.algoAsset.getGrid();
        fullBounds.encompass(toCellPos(fullBounds.min.toVector3d(), grid));
        fullBounds.encompass(toCellPos(fullBounds.max.clone().subtract(grid.clone()).toVector3d(), grid));

        Map<Long, Vector3i> borderPositions = new LinkedHashMap<>();
        for (var entry : baseWave.keySet()) {
            for (int dir = 0; dir < 4; dir++) {
                Vector3i neighbor = GridWave.getNeighborPos(entry, dir, argument);
                switch (borderType){
                    case OuterBorder: if (!fullBounds.contains(neighbor)) borderPositions.put(getEdgeKey(entry, neighbor), neighbor); break;
                    case InnerBorder: if (!baseWave.containsKey(neighbor)) borderPositions.put(getEdgeKey(entry, neighbor), neighbor); break;
                }
            }
        }

        for(var entry : borderPositions.entrySet()){
            RuleCombo ruleCombo = borderRuleSets[new Random(entry.getKey()).nextInt()%borderRuleSets.length].build();
            TileSet.TileEntry tileEntry = new TileSet.TileEntry(Map.of(Vector3i.ZERO,ruleCombo),Vector3i.ZERO,1,0, MirrorDirection.None, null, new ArrayList<>());
            WaveCell waveCell = new WaveCell(entry.getValue().clone(), entry.getValue().clone(), tileEntry, GridTileType.BASIC);
            GridWave.propagate(waveCell, baseWave, null, argument);
        }
    }

    private static long getEdgeKey(Vector3i entry, Vector3i neighbor) {
        return ((long) (entry.x + neighbor.x + 1024) << 42) | ((long) (entry.y + neighbor.y + 1024) << 22) | ((long) (entry.z + neighbor.z + 1024) << 2);
    }
}
