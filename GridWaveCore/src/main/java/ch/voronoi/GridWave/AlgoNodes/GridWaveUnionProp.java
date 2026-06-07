package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.DebugUtils;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.props.OffsetProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static ch.voronoi.GridWave.AlgoNodes.Helper.GridTile.getAllPossiblePropVariants;

public class GridWaveUnionProp extends Prop {
    private static final ConcurrentHashMap<String, List<GridTile>> tileListCache = new ConcurrentHashMap<>();

    @Nonnull private final List<Vector3dc> gridPositions;
    @Nonnull private final List<TileSet> poiTileEntries;
    @Nonnull private final List<TileSet> baseTileEntries;
    @Nonnull private final List<TileSet> fancyTileEntries;
    @Nonnull private final TileSetAsset.Argument argument;

    @Nonnull private final Bounds3i readBounds_voxelGrid;
    @Nonnull private final Bounds3i writeBounds_voxelGrid;

    public GridWaveUnionProp(@Nonnull List<Vector3dc> gridPositions, @Nonnull List<TileSet> poiTileEntries, @Nonnull List<TileSet> baseTileEntries, @Nonnull List<TileSet> fancyTileEntries, @Nonnull TileSetAsset.Argument argument) {
        this.gridPositions = gridPositions;
        this.poiTileEntries = poiTileEntries;
        this.baseTileEntries = baseTileEntries;
        this.fancyTileEntries = fancyTileEntries;
        this.argument = argument;

        tileListCache.keySet().removeIf(key -> key.startsWith(argument.seedBox.toString().substring(0, argument.seedBox.toString().length()-2)));

        List<Prop> props = Stream.of(poiTileEntries, baseTileEntries, fancyTileEntries)
                .flatMap(Collection::stream)
                .flatMap(TileSet::getTileEntries)
                .map(TileEntry::getEntryPropFunction)
                .flatMap(function -> getAllPossiblePropVariants(function, argument))
                .toList();

        Bounds3i readBounds_voxelGrid = new Bounds3i();
        Bounds3i writeBounds_voxelGrid = new Bounds3i();

        for (Prop prop : props) {
            readBounds_voxelGrid.encompass(prop.getReadBounds_voxelGrid());
            writeBounds_voxelGrid.encompass(prop.getWriteBounds_voxelGrid());
        }

        this.readBounds_voxelGrid = new Bounds3i();
        this.writeBounds_voxelGrid = new Bounds3i();

        for (Vector3dc pos : gridPositions){
            this.readBounds_voxelGrid.encompass(readBounds_voxelGrid.clone().offset(Vector3dUtil.toVector3i((Vector3d)pos)));
            this.readBounds_voxelGrid.encompass(readBounds_voxelGrid.clone().offsetOpposite(Vector3dUtil.toVector3i((Vector3d)pos)));

            this.writeBounds_voxelGrid.encompass(writeBounds_voxelGrid.clone().offset(Vector3dUtil.toVector3i((Vector3d)pos)));
            this.writeBounds_voxelGrid.encompass(writeBounds_voxelGrid.clone().offsetOpposite(Vector3dUtil.toVector3i((Vector3d)pos)));
        }
    }

    @Override
    public boolean generate(@Nonnull Prop.Context context) {
        boolean hasGenerated = false;

        TileSetAsset.Argument subArgument = new TileSetAsset.Argument(
                argument.parentSeed,
                argument.materialCache,
                argument.referenceBundle,
                argument.workerId,
                argument.seedBox.child(DebugUtils.VectorStr(context.position)),
                getWriteBounds_voxelGrid(),
                argument.algoAsset
        );

        List<GridTile> tiles = tileListCache.computeIfAbsent(subArgument.seedBox.toString(), k -> {
            List<GridTile> gridTiles = GridWave.solve(gridPositions, poiTileEntries, baseTileEntries, fancyTileEntries, subArgument);
            return gridTiles.isEmpty() ? null : gridTiles;
        });

        if (tiles == null) return false;

        Map<Vector3d, Prop> gridProps = loadPrefabProps(tiles, subArgument);
        Map<Vector3d, Prop> extraProps = new LinkedHashMap<>();
        argument.algoAsset.getFeatureAssets().forEach(x -> extraProps.putAll(x.GetExtraProps(subArgument.algoAsset.getGridBounds(), argument)));

        List<Prop> props = new ArrayList<>();
        for (var entry : gridProps.entrySet()) {
            props.add(new OffsetProp(Vector3dUtil.toVector3i(entry.getKey()), entry.getValue()));
        }
        for (var entry : extraProps.entrySet()) {
            props.add(new OffsetProp(Vector3dUtil.toVector3i(entry.getKey()), entry.getValue()));
        }

        for (Prop prop : props) {
            hasGenerated |= prop.generate(context);
        }

        return hasGenerated;
    }

    @NonNullDecl
    @Override
    public Bounds3i getReadBounds_voxelGrid() {
        return this.readBounds_voxelGrid;
    }

    @Nonnull
    @Override
    public Bounds3i getWriteBounds_voxelGrid() {
        return this.writeBounds_voxelGrid;
    }


    public static @NonNull Map<Vector3d, Prop> loadPrefabProps(List<GridTile> gridTiles, TileSetAsset.Argument argument) { //Rename
        Map<Vector3d, Prop> gridProps = new LinkedHashMap<>();
        for (var gridTile : gridTiles) {
            if (gridTile == null) continue;
            var result = gridTile.getFullPropFunction();
            gridProps.put(Vector3iUtil.toVector3d(gridTile.gridPosition()),result.apply(argument));
        }
        return gridProps;
    }
}
