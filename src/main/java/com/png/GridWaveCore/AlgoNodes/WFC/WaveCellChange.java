package com.png.GridWaveCore.AlgoNodes.WFC;

import com.hypixel.hytale.math.vector.Vector3i;

public record WaveCellChange(Vector3i pos, WaveCell cell) {
    public WaveCellChange(Vector3i pos, WaveCell cell) {
        this.pos = pos;
        this.cell = cell == null ? null : new WaveCell(cell);
    }
}
