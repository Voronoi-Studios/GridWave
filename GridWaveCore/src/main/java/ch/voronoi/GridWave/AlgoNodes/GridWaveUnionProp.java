package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.DebugUtils;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.SectionData;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.hytalegenerator.props.OffsetProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class GridWaveUnionProp extends Prop {
    private static final ConcurrentHashMap<String, List<GridTile>> tileListCache = new ConcurrentHashMap<>();

    @Nonnull private final List<Vector3d> gridPositions;
    @Nonnull private final List<TileSet.TileEntry> poiTileEntries;
    @Nonnull private final List<TileSet.TileEntry> baseTileEntries;
    @Nonnull private final List<TileSet.TileEntry> fancyTileEntries;
    @Nonnull private final TileSetAsset.Argument argument;

    @Nonnull private final Bounds3i readBounds_voxelGrid;
    @Nonnull private final Bounds3i writeBounds_voxelGrid;

    public GridWaveUnionProp(@Nonnull List<Vector3d> gridPositions, @Nonnull List<TileSet.TileEntry> poiTileEntries, @Nonnull List<TileSet.TileEntry> baseTileEntries, @Nonnull List<TileSet.TileEntry> fancyTileEntries, @Nonnull TileSetAsset.Argument argument) {
        this.gridPositions = gridPositions;
        this.poiTileEntries = poiTileEntries;
        this.baseTileEntries = baseTileEntries;
        this.fancyTileEntries = fancyTileEntries;
        this.argument = argument;

        tileListCache.keySet().removeIf(key -> key.startsWith(argument.seedBox.toString().substring(0, argument.seedBox.toString().length()-2)));

        List<Prop> props = Stream.of(poiTileEntries, baseTileEntries, fancyTileEntries)
                .flatMap(Collection::stream)
                .map(TileSet.TileEntry::propFunction)
                .filter(Objects::nonNull)
                .map(x -> x.apply(argument))
                .toList();

        Bounds3i readBounds_voxelGrid = new Bounds3i();
        Bounds3i writeBounds_voxelGrid = new Bounds3i();

        for (Prop prop : props) {
            readBounds_voxelGrid.encompass(prop.getReadBounds_voxelGrid());
            writeBounds_voxelGrid.encompass(prop.getWriteBounds_voxelGrid());
        }

        Vector3i gridMin = smallest(gridPositions);
        Vector3i gridMax = biggest(gridPositions);
        Vector3i gridCenter = gridMax.clone().subtract(gridMin.clone()).scale(0.5);

        this.readBounds_voxelGrid = new Bounds3i();
        this.readBounds_voxelGrid.encompass(readBounds_voxelGrid.clone().offsetOpposite(gridCenter));
        this.readBounds_voxelGrid.encompass(readBounds_voxelGrid.clone().offset(gridCenter));

        this.writeBounds_voxelGrid = new Bounds3i();
        this.writeBounds_voxelGrid.encompass(writeBounds_voxelGrid.clone().offsetOpposite(gridCenter));
        this.writeBounds_voxelGrid.encompass(writeBounds_voxelGrid.clone().offset(gridCenter));
    }

    @Override
    public boolean generate(@Nonnull Prop.Context context) {
        boolean hasGenerated = false;

        TileSetAsset.Argument subArgument = new TileSetAsset.Argument(argument.parentSeed,argument.materialCache, argument.referenceBundle, argument.workerId, argument.seedBox.child(DebugUtils.VectorStr(context.position)), argument.algoAsset);

        List<GridTile> tiles = tileListCache.computeIfAbsent(subArgument.seedBox.toString(), k -> {
            List<GridTile> gridTiles = GridWave.solve(gridPositions, poiTileEntries, baseTileEntries, fancyTileEntries, subArgument);
            return gridTiles.isEmpty() ? null : gridTiles;
        });

        if (tiles == null) return false;

        Map<Vector3d, Prop> gridProps = GridWave.loadPrefabProps(tiles, subArgument);
        List<Prop> props = new ArrayList<>();
        for (var entry : gridProps.entrySet()) {
            props.add(new OffsetProp(entry.getKey().toVector3i().clone(), entry.getValue()));
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

    private static Vector3i smallest(List<Vector3d> positions) {
        return positions.stream().reduce(
                new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE),
                (a, b) -> new Vector3d(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z))
        ).toVector3i();
    }

    private static Vector3i biggest(List<Vector3d> positions) {
        return positions.stream().reduce(
                new Vector3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                (a, b) -> new Vector3d(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z))
        ).toVector3i();
    }
}
