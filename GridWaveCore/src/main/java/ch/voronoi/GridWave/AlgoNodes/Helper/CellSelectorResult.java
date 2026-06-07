package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.Helper.EarlyExitReason;

public record CellSelectorResult(
        WaveCell selectedCell,
        EarlyExitReason earlyExitReason
) {}

