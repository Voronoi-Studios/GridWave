package com.png.GridWaveCore.AlgoNodes.Helper;

import org.joml.Vector3i;
import org.joml.Vector3ic;

public record WaveCellChange(Vector3ic pos, WaveCell cell) {
    public WaveCellChange(Vector3ic pos, WaveCell cell) {
        this.pos = pos;
        this.cell = cell == null ? null : new WaveCell(cell);
    }
}
