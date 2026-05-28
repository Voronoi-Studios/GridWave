package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.ConditionalWeightFeatureAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import org.joml.Vector3ic;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;

public class WaveCell {
    private final Vector3ic gridPosition;
    private final Vector3ic actualPosition;
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
    public Vector3ic getGridPosition() { return gridPosition; }
    public Vector3ic getActualPosition() { return actualPosition; }




    public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry,actualPosition, type, connectedPOIs);
        possible = null;
    }

    public void collapse(Random randomSupplier, Map<Vector3ic, WaveCell> wave, TileSetAsset.Argument argument) {
        var weightedMap = new WeightedMap<TileSet.TileEntry>();
        for (TileSet.TileEntry t : possible) {
            weightedMap.add(t, t.getWeight(wave, argument)); //Is this expensive?
        }
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }

    public WaveCell(SectionData.Entry entry) {
        this.gridPosition = entry.position;
        this.actualPosition = entry.position;
        setChosen(new TileSet.TileEntry(entry), entry.gridTileType);
    }

    public WaveCell(Vector3ic gridPos, Vector3ic actualPos, TileSet.TileEntry tile, GridTileType type) {
        this.gridPosition = gridPos;
        this.actualPosition = actualPos;
        setChosen(tile, type);
    }

    public WaveCell(Vector3ic gridPos, Vector3ic actualPos, LinkedHashSet<TileSet.TileEntry> possible) {
        this.gridPosition = gridPos;
        this.actualPosition = actualPos;
        this.possible = new LinkedHashSet<>(possible);
    }

    public WaveCell(WaveCell other) {
        this.gridPosition = other.gridPosition == null ? null : other.gridPosition;
        this.actualPosition = other.actualPosition == null ? null : other.actualPosition;
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.actualPosition(), other.chosen.type(), new LinkedHashSet<>(other.connectedPOIs));
        this.connectedPOIs = new LinkedHashSet<>(other.connectedPOIs);
    }


}
