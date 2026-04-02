package com.png.GridWaveCore.TileNodes;

import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FixedTileSet extends MultiTileSet {


    public FixedTileSet(@Nonnull TileSet tileSet, @Nonnull Vector3i position, int rotation){
        TileSet.TileEntry tileEntry = tileSet.getTileEntries().get(rotation % tileSet.getAllTileEntries().size());
        super(new ArrayList<>(List.of(tileEntry)));
        super.tileEntries.replaceAll(entry -> offsetTileEntry(entry, position));
        //Should only be one. Maybe add check?
    }
}
