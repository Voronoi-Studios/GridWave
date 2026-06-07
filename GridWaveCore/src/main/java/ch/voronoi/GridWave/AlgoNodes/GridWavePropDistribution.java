package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.FeatureNodes.SectionStorageAsset;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.voronoi.GridWave.AlgoNodes.Helper.GridTile.getAllPossiblePropVariants;

public class GridWavePropDistribution extends PropDistribution {

    @Nonnull private final PositionProvider positionProvider;
    @Nonnull private final List<TileSet> poiTileEntries;
    @Nonnull private final List<TileSet> baseTileEntries;
    @Nonnull private final List<TileSet> fancyTileEntries;
    @Nonnull private final TileSetAsset.Argument argument;

    @Nonnull private final SectionStorageAsset.Context sectionStorageContext;

    public GridWavePropDistribution(
            @Nonnull PositionProvider positionProvider,
            @Nonnull List<TileSet> poiTileEntries,
            @Nonnull List<TileSet> baseTileEntries,
            @Nonnull List<TileSet> fancyTileEntries,
            @Nonnull TileSetAsset.Argument argument)
    {
        this.positionProvider = positionProvider;
        this.poiTileEntries = poiTileEntries;
        this.baseTileEntries = baseTileEntries;
        this.fancyTileEntries = fancyTileEntries;
        this.argument = argument;

        this.sectionStorageContext = argument.getSectionStorageContext();
        this.sectionStorageContext.reset();
    }

    //Enumerates all props that could ever come from this distribution for bounds pre-calculation.
    @Override
    public void forEachPossibleProp(@NonNull Consumer<Prop> consumer) {
        Stream.of(poiTileEntries, baseTileEntries, fancyTileEntries)
                .flatMap(Collection::stream)
                .flatMap(TileSet::getTileEntries)
                .map(TileEntry::getEntryPropFunction)
                .flatMap(function -> getAllPossiblePropVariants(function, argument))
                .forEach(consumer);
    }

    @Override
    public void distribute(@Nonnull PropDistribution.Context context) {
        Control control = new Control();

        Bounds3i fullBounds = argument.algoAsset.getGridBounds();
        Vector3i boundsMin = Vector3iUtil.max(Vector3dUtil.toVector3i(context.bounds.min), new Vector3i(fullBounds.min));
        Vector3i boundsMax = Vector3iUtil.min( Vector3dUtil.toVector3i(context.bounds.max), new Vector3i(fullBounds.max));

        if (boundsMin.x > boundsMax.x || boundsMin.y > boundsMax.y || boundsMin.z > boundsMax.z) return;

        Vector3ic grid = argument.algoAsset.getGrid();

        for(int x = boundsMin.x; x <= boundsMax.x; x++){
            for(int z = boundsMin.z; z <= boundsMax.z; z++){
                for(int y = boundsMin.y; y <= boundsMax.y; y++){
                    if (control.stop) break;
                    if (x % grid.x() != 0 || y % grid.y() != 0 || z % grid.z() != 0) continue;
                    Vector3d pos = new Vector3d(x,y,z);
                    Prop prop = getActualProp(pos, sectionStorageContext);
                    if(prop == EmptyProp.INSTANCE) continue;
                    context.pipe.accept(pos, prop, control);
                }
            }
        }
    }

    private Prop getActualProp(Vector3d pos, SectionStorageAsset.Context sectionStorageContext) {
        var entry = sectionStorageContext.getEntry(pos,this::solveSection);
        if (entry == null || entry.propFunction == null) return EmptyProp.INSTANCE;
        return entry.propFunction.apply(argument);
    }

    private List<GridTile> solveSection(Bounds3i bounds) {
        List<Vector3dc> gridPositions = GridWave.getPositions(this.positionProvider, bounds, this.argument.algoAsset.getMaxPositionsCount());
        TileSetAsset.Argument subArgument = new TileSetAsset.Argument(this.argument, bounds);
        return GridWave.solve(gridPositions, this.poiTileEntries, this.baseTileEntries, this.fancyTileEntries, subArgument);
    }
}
