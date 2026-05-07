package ch.voronoi.GridWave.AlgoNodes.Helper;

public record CellSelectiorResult(
        WaveCell selectedCell,
        EarlyExitReason earlyExitReason
) {}

