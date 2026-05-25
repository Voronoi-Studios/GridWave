package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.List;

public interface IAlgoAsset {
    int getMaxPositionsCount();
    List<FeatureAsset> getFeatureAssets();
    Vector3i getGrid();
    Bounds3i getFullBounds();
}
