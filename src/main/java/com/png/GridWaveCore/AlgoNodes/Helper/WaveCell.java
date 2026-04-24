package com.png.GridWaveCore.AlgoNodes.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.png.GridWaveCore.TileNodes.TileSet;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;

public class WaveCell {
    private final Vector3ic position;
    public LinkedHashSet<TileSet.TileEntry> possible;
    private GridTile chosen;
    public LinkedHashSet<Vector3ic> connectedPOIs = new LinkedHashSet<>();

    public GridTile getChosen() { return chosen; }
    public boolean isCollapsed() { return chosen != null; }
    public int getEntropy() { return possible == null ? 0 : possible.size(); }
    public Vector3ic getPosition() { return position; }

    public void collapse(Random randomSupplier) {
        var weightedMap = new WeightedMap<TileSet.TileEntry>();
        possible.forEach(t -> weightedMap.add(t, t.weight()));
        setChosen(weightedMap.pick(randomSupplier), GridTileType.BASIC);
    }

    public WaveCell(Vector3ic pos, TileSet.TileEntry tile, GridTileType type) {
        this.position = new Vector3i(pos);
        setChosen(tile,type);
    }

    public void setChosen(TileSet.TileEntry tileEntry, GridTileType type){
        chosen = new GridTile(tileEntry, position, type);
        possible = null;
    }

    public WaveCell(Vector3ic pos, LinkedHashSet<TileSet.TileEntry> possible) {
        this.position = pos;
        this.possible = new LinkedHashSet<>(possible);
    }

    public WaveCell(WaveCell other) {
        this.position = other.position == null ? null : other.position;
        this.possible = other.possible == null ? null : new LinkedHashSet<>(other.possible);
        this.chosen = other.chosen == null? null : new GridTile(other.chosen.tileEntry(),other.chosen.positionOffset(), other.chosen.type());
    }
}
