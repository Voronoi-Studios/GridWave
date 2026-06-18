package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.ConditionalWeightFeatureAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;

public class WaveCell {
    private final Vector3ic gridPosition;
    private final Vector3ic additionalOffset;
    public LinkedHashSet<TileEntry> possible;
    private GridTile chosen;
    public LinkedHashSet<POIInfo> connectedPOIs = new LinkedHashSet<>();

    public WaveCell(SectionData.Entry entry) {
        this.gridPosition = entry.position;
        this.additionalOffset = Vector3iUtil.ZERO;
        setChosen(new TileEntry(entry), entry.gridTileType);
    }
    public WaveCell(Vector3ic gridPos, Vector3ic actualPos, TileEntry tile, GridTileType type) {
        this.gridPosition = gridPos;
        this.additionalOffset = new Vector3i(actualPos).sub(actualPos);;
        setChosen(tile, type);
    }
    public WaveCell(Vector3ic gridPos, Vector3ic actualPos, LinkedHashSet<TileEntry> possible) {
        this.gridPosition = gridPos;
        this.additionalOffset = new Vector3i(actualPos).sub(actualPos);
        this.possible = new LinkedHashSet<>(possible);
    }
    public WaveCell(WaveCell other) {
        this.gridPosition = other.gridPosition == null ? null : new Vector3i(other.gridPosition);
        this.additionalOffset = other.additionalOffset == null ? Vector3iUtil.ZERO : new Vector3i(other.additionalOffset);
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(), other.chosen.type(), other.chosen.gridPosition(), other.chosen.additionalOffset(), new LinkedHashSet<>(other.connectedPOIs));
        this.connectedPOIs = new LinkedHashSet<>(other.connectedPOIs);
    }

    public Vector3ic getGridPosition() { return gridPosition; }

    public GridTile getChosen() { return chosen; }
    public void setChosen(TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry, type, gridPosition, additionalOffset, connectedPOIs);
        possible = null;
    }

    public int getEntropy() {
        if (possible == null) return 0;
        return possible.stream().filter(x -> x.tileFeatures().stream()
                .noneMatch(f -> f instanceof ConditionalWeightFeatureAsset cW && cW.weightIfTrue == 0))
                .toList().size();
    }

    public boolean isCollapsed() { return chosen != null; }
    public void collapse(Random randomSupplier, Map<Vector3ic, WaveCell> wave, TileSetAsset.Argument argument) {
        var weightedMap = new WeightedMap<TileEntry>();
        for (TileEntry t : possible) {
            weightedMap.add(t, t.getWeight(wave, argument)); //Is this expensive?
        }
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }




}
