package ch.voronoi.GridWave.AlgoNodes.Helper;

public record CellSelectorResult(
        WaveCell selectedCell,
        EarlyExitReason earlyExitReason
) {}

