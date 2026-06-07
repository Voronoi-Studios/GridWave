package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.FeatureNodes.Helper.BorderType;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

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
    public void BaseWaveProcessor(@NonNull Map<Vector3ic, WaveCell> baseWave, TileSetAsset.Argument argument) {
        if(skip() || borderRuleSets.length == 0) return;
        Bounds3i gridBounds = argument.algoAsset.getGridBounds().clone();
        Vector3ic grid = argument.algoAsset.getGrid();
        //gridBounds.encompass(toCellPos(Vector3iUtil.toVector3d(gridBounds.min), grid));
        //gridBounds.encompass(toCellPos(Vector3iUtil.toVector3d(new Vector3i(gridBounds.max).sub(new Vector3i(grid))), grid));

        Map<Long, Vector3ic> borderPositions = new LinkedHashMap<>();
        for (var entry : baseWave.keySet()) {
            for (int dir = 0; dir < 4; dir++) {
                Vector3ic neighbor = GridWave.getNeighborPos(entry, dir, argument);
                switch (this.borderType){
                    case OuterBorder: if (!gridBounds.contains((Vector3i)neighbor)) borderPositions.put(getEdgeKey(entry, neighbor), neighbor); break;
                    case InnerBorder: if (!baseWave.containsKey(neighbor)) borderPositions.put(getEdgeKey(entry, neighbor), neighbor); break;
                }
            }
        }

        var ruleCombos = Arrays.stream(borderRuleSets).flatMap(x -> x.build().stream()).toList();
        for(var entry : borderPositions.entrySet()){
            int key = MathUtil.abs(argument.seedBox.child(entry.getKey().toString()).createSupplier().get());
            RuleCombo ruleCombo = ruleCombos.get(key%ruleCombos.size());
            TileEntry tileEntry = new TileEntry(ruleCombo);
            WaveCell waveCell = new WaveCell(new Vector3i(entry.getValue()), new Vector3i(entry.getValue()), tileEntry, GridTileType.BASIC);
            GridWave.propagate(waveCell, baseWave, null, argument);
        }
    }

    private static long getEdgeKey(Vector3ic entry, Vector3ic neighbor) {
        return ((long) (entry.x() + neighbor.x() + 1024) << 42) | ((long) (entry.y() + neighbor.y() + 1024) << 22) | ((long) (entry.z() + neighbor.z() + 1024) << 2);
    }
}
