package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTile;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.SectionData;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.MirrorNode.Helper.MirrorDirection;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SectionStorageAsset extends FeatureAsset {
    public static final ConcurrentHashMap<String, ConcurrentHashMap<Vector3i, SectionData>> cacheRegistry = new ConcurrentHashMap<>();

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

    public record Context(@Nonnull Vector3i sectionSize, @Nonnull ConcurrentHashMap<Vector3i, SectionData> sectionCache){

        public SectionData.Entry getEntry(@Nonnull Vector3d pos, @Nullable Function<Bounds3i, List<GridTile>> solver) {
            Vector3i sectionKey = getSectionKeyFrom(pos);
            SectionData sectionData = solver == null ? new SectionData() : sectionCache.computeIfAbsent(sectionKey, k -> {
                Bounds3i bounds = getBounds(sectionKey);
                List<GridTile> gridTiles = solver.apply(bounds);
                return new SectionData(gridTiles);
            });
            return sectionData.getEntry(pos);
        }

        @Nonnull
        public Vector3i getSectionKeyFrom(@Nonnull Vector3d pointer) {
            Vector3i address = pointer.toVector3i();
            address.x = sectionFloor(address.x, this.sectionSize.x) / this.sectionSize.x;
            address.y = sectionFloor(address.y, this.sectionSize.y) / this.sectionSize.y;
            address.z = sectionFloor(address.z, this.sectionSize.z) / this.sectionSize.z;
            return address;
        }
        public static int sectionFloor(int voxelAddress, int axisSize) {
            return voxelAddress < 0 ? voxelAddress - voxelAddress % axisSize - axisSize : voxelAddress - voxelAddress % axisSize;
        }
        private @NonNull Vector3i voxelScale(Vector3i sectionAddress) {
            return sectionAddress.clone().scale(sectionSize.clone());
        }

        public Bounds3i getBounds(Vector3i sectionAddress) {
            return new Bounds3i(voxelScale(sectionAddress), voxelScale(sectionAddress).clone().add(sectionSize.clone()));
        }

        public void reset() {
            this.sectionCache.clear();
        }
    }
}
