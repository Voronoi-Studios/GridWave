package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.SectionData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SectionStorageAsset extends FeatureAsset {
    public static final ConcurrentHashMap<String, ConcurrentHashMap<Vector3ic, SectionData>> cacheRegistry = new ConcurrentHashMap<>();

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
    private int horizontalSectionSize = 150;
    private int verticalSectionSize = 640;
    private Vector3i getSectionSize() { return new Vector3i(this.horizontalSectionSize, this.verticalSectionSize, this.horizontalSectionSize);}

    public Context getContext(String key) {
        return new Context(getSectionSize(),cacheRegistry.computeIfAbsent(key, k -> new ConcurrentHashMap<>()));
    }

    public record Context(@Nonnull Vector3ic sectionSize, @Nonnull ConcurrentHashMap<Vector3ic, SectionData> sectionCache){

        public SectionData.Entry getEntry(@Nonnull Vector3d pos, @Nullable Function<Bounds3i, List<GridTile>> solver) {
            Vector3ic sectionKey = getSectionKeyFrom(pos);
            SectionData sectionData = solver == null ? new SectionData() : sectionCache.computeIfAbsent(sectionKey, k -> {
                Bounds3i bounds = getBounds((Vector3i)sectionKey);
                List<GridTile> gridTiles = solver.apply(bounds);
                return new SectionData(gridTiles);
            });
            return sectionData.getEntry(pos);
        }

        @Nonnull
        public Vector3ic getSectionKeyFrom(@Nonnull Vector3d pointer) {
            Vector3i address = Vector3dUtil.toVector3i(pointer);
            address.x = sectionFloor(address.x, this.sectionSize.x()) / this.sectionSize.x();
            address.y = sectionFloor(address.y, this.sectionSize.y()) / this.sectionSize.y();
            address.z = sectionFloor(address.z, this.sectionSize.z()) / this.sectionSize.z();
            return address;
        }
        public static int sectionFloor(int voxelAddress, int axisSize) {
            return voxelAddress < 0 ? voxelAddress - voxelAddress % axisSize - axisSize : voxelAddress - voxelAddress % axisSize;
        }
        private @NonNull Vector3ic voxelScale(Vector3ic sectionAddress) {
            return new Vector3i(sectionAddress).mul(sectionSize);
        }

        public Bounds3i getBounds(Vector3ic sectionAddress) {
            return new Bounds3i(voxelScale(sectionAddress), new Vector3i(voxelScale(sectionAddress)).add(sectionSize));
        }

        public void reset() {
            this.sectionCache.clear();
        }
    }
}
