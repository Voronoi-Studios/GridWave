package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;

public class GridGen extends PositionProvider {
    private final Vector3ic pos;
    private final Vector3ic offset;
    private final Vector3ic repeat;
    private final boolean centeredOnPosition;

    private static final double Y = 0.0;
    @Nonnull
    private final Vector3d rPosition = new Vector3d();
    @Nonnull
    private final Bounds3d rGridBounds = new Bounds3d();
    @Nonnull
    private final Control rControl = new Control();

    public GridGen(Vector3ic pos, Vector3ic offset, Vector3ic repeat, boolean centeredOnPosition) {
        this.pos = pos;
        this.offset = Vector3iUtil.max(Vector3iUtil.ALL_ONES,offset);
        this.repeat = repeat;
        this.centeredOnPosition = centeredOnPosition;
    }

    @Override
    public void generate(@NonNullDecl PositionProvider.Context context) {
        if (!(context.bounds.min.y > 0.0) && !(context.bounds.max.y <= 0.0)) {
            this.rGridBounds.min.set(context.bounds.min.x, context.bounds.min.y, context.bounds.min.z);
            this.rGridBounds.max.set(context.bounds.max.x, context.bounds.max.y, context.bounds.max.z);
            if (this.rGridBounds.min.x >= rGridBounds.max.x) {
                this.rGridBounds.max.x = this.rGridBounds.min.x + 1;
            }
            if (this.rGridBounds.min.y >= rGridBounds.max.y) {
                this.rGridBounds.max.y = this.rGridBounds.min.y + 1;
            }
            if (this.rGridBounds.min.z >= rGridBounds.max.z) {
                this.rGridBounds.max.z = this.rGridBounds.min.z + 1;
            }

            this.rGridBounds.intersect(createBounds(pos, offset, repeat, centeredOnPosition).toBounds3d());

            this.rControl.reset();

            for (double x = rGridBounds.min.x; x < rGridBounds.max.x; x += offset.x()) {
                for (double y = rGridBounds.min.y; y < rGridBounds.max.y; y += offset.y()) {
                    for (double z = rGridBounds.min.z; z < rGridBounds.max.z; z += offset.z()) {
                        if (this.rControl.stop) return;

                        this.rPosition.set(x, y, z);
                        context.pipe.accept(this.rPosition, this.rControl);
                    }
                }
            }
        }
    }

    public static Bounds3i createBounds(Vector3ic pos, Vector3ic offset, Vector3ic repeat, boolean centeredOnPosition) {
        Vector3i size = new Vector3i(
                Math.max(1, offset.x() * (repeat.x() - 1) + 1),
                Math.max(1, offset.y() * (repeat.y() - 1) + 1),
                Math.max(1, offset.z() * (repeat.z() - 1) + 1));
        Bounds3i bounds = new Bounds3i(pos, new Vector3i(pos).add(size));
        if (centeredOnPosition) {
            Vector3i half1 = new Vector3i(size.x() / 2, size.y() / 2, size.z() / 2);
            Vector3i half2 = new Vector3i(size.x() - half1.x(), size.y() - half1.y(), size.z() - half1.z());
            bounds = new Bounds3i(new Vector3i(pos).sub(half1),new Vector3i(pos).add(half2));
        }
        bounds.offset(new Vector3i(-offset.x() / 2, -offset.y() / 2, -offset.z() / 2).mul((repeat.x() + 1) % 2, (repeat.y() + 1) % 2,(repeat.z() + 1) % 2));
        bounds.correct();
        return bounds;
    }
}
