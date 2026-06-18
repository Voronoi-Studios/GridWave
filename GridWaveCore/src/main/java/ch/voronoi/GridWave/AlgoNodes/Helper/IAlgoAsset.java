package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import org.joml.Vector3ic;

import java.util.List;

public interface IAlgoAsset extends IFeatureCheck {
    int getMaxPositionsCount();
    Vector3ic getGrid();
    Bounds3i getFullBounds();
    List<TileSet> getPOITileSets(TileSetAsset.Argument argument);
    List<TileSet> getBaseTileSets(TileSetAsset.Argument argument);
    List<TileSet> getFancyTileSets(TileSetAsset.Argument argument);
}
