package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import org.joml.Vector3ic;

public interface IAlgoAsset extends IFeatureCheck {
    int getMaxPositionsCount();
    Vector3ic getGrid();
    Bounds3i getGridBounds();
}
