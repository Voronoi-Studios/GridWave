package com.png.GridWaveCore.AlgoNodes.WFC;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.TileNodes.TileSet;

import java.util.*;

public class WaveCell {
    private Vector3i position;
    public LinkedHashSet<TileSet.TileEntry> possible;
    private GridTile chosen;
    public LinkedHashSet<Vector3i> connectedPOIs;

    public GridTile getChosen() { return chosen; }
    public boolean isCollapsed() { return chosen != null; }
    public int getEntropy() { return possible == null ? 0 : possible.size(); }
    public Vector3i getPosition() { return position.clone(); }

    public void collapse(Random randomSupplier) {
        var weightedMap = new WeightedMap<TileSet.TileEntry>();
        possible.forEach(t -> weightedMap.add(t, t.weight()));
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }

    public WaveCell(Vector3i pos, TileSet.TileEntry tile, GridTileType type) {
        this.position = pos.clone();
        setChosen(tile,type);
    }

    public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry, position.clone(), type);
        possible = null;
    }

    public WaveCell(Vector3i pos, LinkedHashSet<TileSet.TileEntry> possible) {
        this.position = pos.clone();
        this.possible = new LinkedHashSet<>(possible);
    }

    public WaveCell(WaveCell other) {
        this.position = other.position == null ? null : other.position.clone();
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.positionOffset(), other.chosen.type());
    }
}
