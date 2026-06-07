package ch.voronoi.GridWave.Utils.CuboidWireframe;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

public class WireframeCuboidProp extends Prop {
    @Nonnull
    private final Bounds3i bounds;
    @Nonnull
    private final MaterialProvider<Material> materialProvider;
    @Nonnull
    private final Bounds3i rIntersectingBounds;
    @Nonnull
    private final Bounds3i rNewBoundsBounds;
    @Nonnull
    private final MaterialProvider.Context rContext;

    public WireframeCuboidProp(@Nonnull Bounds3i bounds, @Nonnull MaterialProvider<Material> materialProvider) {
        this.bounds = bounds.clone();
        this.materialProvider = materialProvider;
        this.rIntersectingBounds = new Bounds3i();
        this.rNewBoundsBounds = new Bounds3i();
        this.rContext = new MaterialProvider.Context(new Vector3i(), 1.0, 0, 0, 0, 0, null, Double.MAX_VALUE);
    }

    @Override
    public boolean generate(@Nonnull Prop.Context context) {
        this.rNewBoundsBounds.assign(this.bounds);
        this.rNewBoundsBounds.offset(context.position);
        int minYInclusive = this.rNewBoundsBounds.min.y;
        int maxYExclusive = this.rNewBoundsBounds.max.y;
        this.rIntersectingBounds.assign(this.rNewBoundsBounds.clone());
        this.rIntersectingBounds.intersect(context.materialWriteSpace.getBounds());
        Vector3i position = this.rContext.position;
        this.rContext.density = 1.0;

        for (position.x = this.rIntersectingBounds.min.x; position.x < this.rIntersectingBounds.max.x; position.x++) {
            int onFaceX = position.x == this.rNewBoundsBounds.min.x || position.x == this.rNewBoundsBounds.max.x - 1 ? 1 : 0;
            for (position.z = this.rIntersectingBounds.min.z; position.z < this.rIntersectingBounds.max.z; position.z++) {
                int onFaceZ = position.z == this.rNewBoundsBounds.min.z || position.z == this.rNewBoundsBounds.max.z - 1? 1 : 0;
                for (position.y = this.rIntersectingBounds.min.y; position.y < this.rIntersectingBounds.max.y; position.y++) {
                    int onFaceY = position.y == this.rNewBoundsBounds.min.y || position.y == this.rNewBoundsBounds.max.y - 1? 1 : 0;
                    if (onFaceX + onFaceZ + onFaceY < 2) continue;

                    this.rContext.depthIntoFloor = maxYExclusive - position.y;
                    this.rContext.depthIntoCeiling = position.y - minYInclusive;
                    this.rContext.spaceAboveFloor = Integer.MAX_VALUE;
                    this.rContext.spaceBelowCeiling = Integer.MAX_VALUE;
                    Material material = this.materialProvider.getVoxelTypeAt(this.rContext);
                    context.materialWriteSpace.set(material, position);
                }
            }
        }

        return true;
    }

    @NonNullDecl
    @Override
    public Bounds3i getReadBounds_voxelGrid() {
        return Bounds3i.ZERO;
    }

    @NonNullDecl
    @Override
    public Bounds3i getWriteBounds_voxelGrid() {
        return this.bounds;
    }
}
