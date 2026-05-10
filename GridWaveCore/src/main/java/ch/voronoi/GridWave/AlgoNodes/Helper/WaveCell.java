package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.ConditionalWeightFeatureAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.TileSetNodes.TileSet;

import java.util.*;

public class WaveCell {
    private final Vector3i gridPosition;
    private final Vector3i actualPosition;
    public LinkedHashSet<TileSet.TileEntry> possible;
    private GridTile chosen;
    public LinkedHashSet<POIInfo> connectedPOIs = new LinkedHashSet<>();

    public GridTile getChosen() { return chosen; }
    public boolean isCollapsed() { return chosen != null; }
    public int getEntropy() {
        if (possible == null) return 0;
        return possible.stream().filter(x -> x.tileFeatures().stream()
                .noneMatch(f -> f instanceof ConditionalWeightFeatureAsset cW && cW.weightIfTrue == 0))
                .toList().size();
    }
    public Vector3i getGridPosition() { return gridPosition.clone(); }
    public Vector3i getActualPosition() { return actualPosition.clone(); }




    public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry,actualPosition.clone(), type, connectedPOIs);
        possible = null;
    }

    public void collapse(Random randomSupplier, Map<Vector3i, WaveCell> wave, TileSetAsset.Argument argument) {
        var weightedMap = new WeightedMap<TileSet.TileEntry>();
        for (TileSet.TileEntry t : possible) {
            weightedMap.add(t, t.getWeight(wave, argument)); //Is this expensive?
        }
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }

    public WaveCell(Vector3i gridPos, Vector3i actualPos, TileSet.TileEntry tile, GridTileType type) {
        this.gridPosition = gridPos.clone();
        this.actualPosition = actualPos.clone();
        setChosen(tile,type);
    }

    public WaveCell(Vector3i gridPos, Vector3i actualPos, LinkedHashSet<TileSet.TileEntry> possible) {
        this.gridPosition = gridPos.clone();
        this.actualPosition = actualPos.clone();
        this.possible = new LinkedHashSet<>(possible);
    }

    public WaveCell(WaveCell other) {
        this.gridPosition = other.gridPosition == null ? null : other.gridPosition.clone();
        this.actualPosition = other.actualPosition == null ? null : other.actualPosition.clone();
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.actualPosition().clone(), other.chosen.type(), new LinkedHashSet<>(other.connectedPOIs));
        this.connectedPOIs = new LinkedHashSet<>(other.connectedPOIs);
    }


}
