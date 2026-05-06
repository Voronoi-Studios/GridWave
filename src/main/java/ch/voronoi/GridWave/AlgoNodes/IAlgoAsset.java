package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.List;

public interface IAlgoAsset {
    int getPOICount();
    int getMaxPositionsCount();
    List<FeatureAsset> getFeatureAssets();
    Vector3i getGrid();
}
