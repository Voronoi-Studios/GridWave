package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import org.joml.Vector3ic;

import java.util.List;

public interface IAlgoAsset extends IFeatureCheck {
    int getMaxPositionsCount();
    Vector3ic getGrid();
    Bounds3i getFullBounds();
}
