package ch.voronoi.GridWave.AlgoNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.SectionData;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GridWavePropDistribution extends PropDistribution {
    private static final ConcurrentHashMap<String, ConcurrentHashMap<Vector3i, SectionData>> cacheRegistry = new ConcurrentHashMap<>();

    @Nonnull private final PositionProvider positionProvider;
    @Nonnull private final Bounds3i seccondaryBounds;
    @Nonnull private final List<TileSet> poiTileEntries;
    @Nonnull private final List<TileSet> baseTileEntries;
    @Nonnull private final List<TileSet> fancyTileEntries;
    @Nonnull private final TileSetAsset.Argument argument;

    private final int sectionSize;
    private final int cacheSize;

    public GridWavePropDistribution(
            @Nonnull PositionProvider positionProvider,
            @Nonnull Bounds3i secondaryBounds,
            @Nonnull List<TileSet> poiTileEntries,
            @Nonnull List<TileSet> baseTileEntries,
            @Nonnull List<TileSet> fancyTileEntries,
            @Nonnull TileSetAsset.Argument argument)
    {
        this.positionProvider = positionProvider;
        this.seccondaryBounds = secondaryBounds;
        this.poiTileEntries = poiTileEntries;
        this.baseTileEntries = baseTileEntries;
        this.fancyTileEntries = fancyTileEntries;
        this.argument = argument;

        cacheRegistry.keySet().removeIf(key -> key.equals(argument.seedBox.toString()));

        sectionSize = 11*20; //get from features
        cacheSize = 500; //get from features

        if (!(sectionSize > 0 && cacheSize >= 0)) throw new IllegalArgumentException();
    }

    //Enumerates all props that could ever come from this distribution for bounds pre-calculation.
    @Override
    public void forEachPossibleProp(@NonNull Consumer<Prop> consumer) {
        Stream.of(poiTileEntries, baseTileEntries, fancyTileEntries)
                .flatMap(Collection::stream)
                .flatMap(TileSet::getAllTileEntries)
                .map(TileSet.TileEntry::propFunction)
                .filter(Objects::nonNull)
                .forEach(x -> x.apply(argument));
    }

    @Override
    public void distribute(@Nonnull PropDistribution.Context context) {
        Control control = new Control();

        Vector3i boundsMin = Vector3i.max(context.bounds.min.toVector3i(), seccondaryBounds.min.clone());
        Vector3i boundsMax = Vector3i.min(context.bounds.max.toVector3i(), seccondaryBounds.max.clone());

        ConcurrentHashMap<Vector3i, SectionData> sectionCache = cacheRegistry.computeIfAbsent(argument.seedBox.toString(),k -> new ConcurrentHashMap<>());

        for(int x = boundsMin.x; x <= boundsMax.x; x++){
            for(int z = boundsMin.z; z <= boundsMax.z; z++){
                for(int y = boundsMin.y; y <= boundsMax.y; y++){
                    if (control.stop) break;
                    Vector3d pos = new Vector3d(x,y,z);
                    Prop prop = getActualProp(pos, sectionCache);
                    if(prop == EmptyProp.INSTANCE) continue;
                    context.pipe.accept(pos, prop, control);
                }
            }
        }
    }

    private Prop getActualProp(Vector3d pos, ConcurrentHashMap<Vector3i, SectionData> sectionCache) {
        Vector3i sectionAddress = sectionAddress(pos);
        SectionData sectionData = sectionCache.computeIfAbsent(sectionAddress, k -> solveSection(sectionAddress));
        var entry = sectionData.getEntry(pos);
        if (entry == null || entry.propFunction == null) return EmptyProp.INSTANCE;
        return entry.propFunction.apply(argument);
    }

    @Nonnull
    private Vector3i sectionAddress(@Nonnull Vector3d pointer) { //Wrong
        Vector3i address = pointer.toVector3i();
        address.x = sectionFloor(address.x) / this.sectionSize;
        address.y = sectionFloor(address.y) / this.sectionSize;
        address.z = sectionFloor(address.z) / this.sectionSize;
        return address;
    }
    public int sectionFloor(int voxelAddress) {
        return voxelAddress < 0 ? voxelAddress - voxelAddress % this.sectionSize - this.sectionSize : voxelAddress - voxelAddress % this.sectionSize;
    }

    private SectionData solveSection(Vector3i sectionAddress) {
        Bounds3i bounds = new Bounds3i(sectionAddress.clone().scale(this.sectionSize), sectionMax(sectionAddress.clone().scale(this.sectionSize)));
        List<Vector3d> gridPositions = GridWave.getPositions(this.positionProvider, bounds, this.argument.algoAsset.getMaxPositionsCount());
        TileSetAsset.Argument subArgument = new TileSetAsset.Argument(this.argument, bounds);
        List<GridTile> gridTiles = GridWave.solve(gridPositions, this.poiTileEntries, this.baseTileEntries, this.fancyTileEntries, subArgument);
        return new SectionData(gridTiles);
    }

    @Nonnull
    private Vector3i sectionMax(@Nonnull Vector3i sectionAddress) { //floor from sectionAddress
        Vector3i max = sectionAddress.clone();
        max.x = max.x + this.sectionSize;
        max.y = max.y + this.sectionSize;
        max.z = max.z + this.sectionSize;
        return max;
    }
}
