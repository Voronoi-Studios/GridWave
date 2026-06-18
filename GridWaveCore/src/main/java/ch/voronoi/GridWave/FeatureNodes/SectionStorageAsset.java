package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.SectionData;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SectionStorageAsset extends FeatureAsset {
    public static final ConcurrentHashMap<String, ConcurrentHashMap<Vector3ic, CompletableFuture<SectionData>>> cacheRegistry = new ConcurrentHashMap<>();

    @Nonnull
    public static final BuilderCodec<SectionStorageAsset> CODEC = BuilderCodec.builder(
                    SectionStorageAsset.class, SectionStorageAsset::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("WriteToWorldFolder", Codec.BOOLEAN), (asset, v) -> asset.writeToWorldFolder = v, asset -> asset.writeToWorldFolder)
            .add()
            .append(new KeyedCodec<>("CacheSize", Codec.INTEGER), (asset, v) -> asset.cacheSize = v, asset -> asset.cacheSize)
            .add()
            .append(new KeyedCodec<>("HorizontalSectionSize", Codec.INTEGER), (asset, v) -> asset.horizontalSectionSize = v, asset -> asset.horizontalSectionSize)
            .add()
            .append(new KeyedCodec<>("VerticalSectionSize", Codec.INTEGER), (asset, v) -> asset.verticalSectionSize = v, asset -> asset.verticalSectionSize)
            .add()
            .build();

    private boolean writeToWorldFolder = false;
    private int cacheSize = 50;
    private int horizontalSectionSize = 15;
    private int verticalSectionSize = 0;

    public Context getNewContext(TileSetAsset.Argument argument) {
        Vector3i sectionSize = new Vector3i(this.horizontalSectionSize, this.verticalSectionSize == 0 ? 640 / argument.algoAsset.getGrid().y() : this.verticalSectionSize, this.horizontalSectionSize);
        Vector3i voxelSectionSize = sectionSize.mul(argument.algoAsset.getGrid());
        return new Context(voxelSectionSize,cacheRegistry.computeIfAbsent(argument.seedBox.toString(), k -> new ConcurrentHashMap<>()), argument);
    }

    public record Context(@Nonnull Vector3ic voxelSectionSize, @Nonnull ConcurrentHashMap<Vector3ic, CompletableFuture<SectionData>> sectionCache, TileSetAsset.Argument argument){

        @Nonnull
        public Vector3ic getSectionKeyFrom(@Nonnull Vector3d pointer) {
            Vector3i address = Vector3dUtil.toVector3i(pointer);
            address.x = Math.floorDiv(address.x, this.voxelSectionSize.x());
            address.y = Math.floorDiv(address.y, this.voxelSectionSize.y());
            address.z = Math.floorDiv(address.z, this.voxelSectionSize.z());
            return address;
        }

        private @NonNull Vector3ic voxelScale(Vector3ic sectionAddress) {
            return new Vector3i(sectionAddress).mul(voxelSectionSize);
        }

        public Bounds3i getBounds(Vector3ic sectionAddress) {
            return new Bounds3i(voxelScale(sectionAddress), new Vector3i(voxelScale(sectionAddress)).add(voxelSectionSize));
        }

        public void reset() {
            this.sectionCache.clear();
        }


        public SectionData.Entry getEntry(@Nonnull Vector3d pos, @Nullable Function<Bounds3i, List<GridTile>> solver) {
            Vector3ic sectionKey = getSectionKeyFrom(pos);
            CompletableFuture<SectionData> target = getOrSolve(sectionKey, solver); //Make sure this returns completed if solver is null

            //target existed already, so we solve neighbors till the target is available
            int dist = 1;
            long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
            while (!target.isDone()){
                if (System.currentTimeMillis() > deadline) return null;
                for (Vector3ic neighborKey : getSurroundingKeys(sectionKey, dist++)) {
                    if (argument.algoAsset.getFullBounds().contains(Vector3iUtil.toVector3d(neighborKey))) continue;
                    if (!sectionCache.containsKey(neighborKey)) {
                        getOrSolve(neighborKey, solver);
                    }
                }
            }

            return target.join().getEntry(pos);
        }

        private CompletableFuture<SectionData> getOrSolve(Vector3ic sectionKey, Function<Bounds3i, List<GridTile>> solver) {
            CompletableFuture<SectionData> future = new CompletableFuture<>();
            CompletableFuture<SectionData> existing = sectionCache.putIfAbsent(sectionKey, future);
            if (existing != null) return existing;
            if (solver == null) future.complete(new SectionData());
            else try {
                Bounds3i bounds = getBounds(sectionKey);
                List<GridTile> gridTiles = solver.apply(bounds);
                future.complete(new SectionData(gridTiles));
            } catch (Exception e) { future.completeExceptionally(e); }

            return future;
        }

        private List<Vector3ic> getSurroundingKeys(Vector3ic origin, int dist) {
            Vector3ic grid = argument.algoAsset.getGrid();

            Vector3ic[] directions = {
                    new Vector3i( grid.x(),0,-grid.z()), //Northeast
                    new Vector3i(-grid.x(),0,-grid.z()), //Northwest
                    new Vector3i(-grid.x(),0, grid.z()), //Southwest
                    new Vector3i( grid.x(),0, grid.z()), //Southeast
            };

            List<Vector3ic> ring = new ArrayList<>();
            Vector3i pos = new Vector3i(origin).add(0, 0, dist * grid.z()); //start at North corner
            for (Vector3ic dir : directions) {
                for (int step = 0; step < dist; step++) {
                    ring.add(new Vector3i(pos));
                    pos.add(dir); //offset pos
                }
            }
            return ring;
        }
    }
}
