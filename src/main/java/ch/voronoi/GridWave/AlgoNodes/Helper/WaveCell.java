package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.ConditionalWeight;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.TileNodes.TileSet;

import java.util.*;

public class WaveCell {
    private Vector3i position;
    public LinkedHashSet<TileSet.TileEntry> possible;
    private GridTile chosen;
    public LinkedHashSet<POIInfo> connectedPOIs = new LinkedHashSet<>();

    public GridTile getChosen() { return chosen; }
    public boolean isCollapsed() { return chosen != null; }
    public int getEntropy() {
        if (possible == null) return 0;
        return possible.stream().filter(x -> x.tileFeatures().stream()
                .noneMatch(f -> f instanceof ConditionalWeight cW && cW.weightIfTrue == 0))
                .toList().size();
    }
    public Vector3i getPosition() { return position.clone(); }

    public WaveCell(Vector3i pos, TileSet.TileEntry tile, GridTileType type) {
        this.position = pos.clone();
        setChosen(tile,type);
    }

    public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry, position.clone(), type, connectedPOIs);
        possible = null;
    }

    public void collapse(Random randomSupplier, Map<Vector3i, WaveCell> wave, TileSetAsset.Argument argument) {
        var weightedMap = new WeightedMap<TileSet.TileEntry>();
        for (TileSet.TileEntry t : possible) {
            weightedMap.add(t, t.getWeight(wave, argument)); //Is this expensive?
        }
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }

    public WaveCell(Vector3i pos, LinkedHashSet<TileSet.TileEntry> possible) {
        this.position = pos.clone();
        this.possible = new LinkedHashSet<>(possible);
    }

    public WaveCell(WaveCell other) {
        this.position = other.position == null ? null : other.position.clone();
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.positionOffset().clone(), other.chosen.type(), new LinkedHashSet<>(other.connectedPOIs));
        this.connectedPOIs = new LinkedHashSet<>(other.connectedPOIs);
    }


}
