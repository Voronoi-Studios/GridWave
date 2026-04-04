package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class GridGen extends PositionProvider {
    private final Vector3i pos;
    private final Vector3i offset;
    private final Vector3i repeat;
    private final boolean centeredOnPosition;
    @Nonnull
    private final Bounds3d rSeccondaryGridBounds = new Bounds3d();

    private static final double Y = 0.0;
    @Nonnull
    private final Vector3d rPosition = new Vector3d();
    @Nonnull
    private final Bounds3d rGridBounds = new Bounds3d();
    @Nonnull
    private final Control rControl = new Control();

    public GridGen(Vector3i pos, Vector3i offset, Vector3i repeat, boolean centeredOnPosition) {
        this.pos = pos;
        this.offset = Vector3i.max(Vector3i.ALL_ONES.clone(),offset);
        this.repeat = repeat;
        this.centeredOnPosition = centeredOnPosition;
    }

    @Override
    public void generate(@NonNullDecl PositionProvider.Context context) {
        if (!(context.bounds.min.y > 0.0) && !(context.bounds.max.y <= 0.0)) {
            this.rGridBounds.min.assign(context.bounds.min.x, context.bounds.min.y, context.bounds.min.z);
            this.rGridBounds.max.assign(context.bounds.max.x, context.bounds.min.y, context.bounds.max.z);
            if (this.rGridBounds.min.x >= rGridBounds.max.x) {
                this.rGridBounds.max.x = this.rGridBounds.min.x + 1;
            }
            if (this.rGridBounds.min.y >= rGridBounds.max.y) {
                this.rGridBounds.max.y = this.rGridBounds.min.y + 1;
            }
            if (this.rGridBounds.min.z >= rGridBounds.max.z) {
                this.rGridBounds.max.z = this.rGridBounds.min.z + 1;
            }

            Vector3i size = new Vector3i(Math.max(1, offset.x * repeat.x), Math.max(1, offset.y * repeat.y), Math.max(1, offset.z * repeat.z));
            if (centeredOnPosition) {
                Vector3i half1 = new Vector3i(size.x / 2, size.y / 2, size.z / 2);
                Vector3i half2 = new Vector3i(size.x - half1.x, size.y - half1.y, size.z - half1.z);
                this.rSeccondaryGridBounds.assign(new Bounds3i(pos.clone().subtract(half1), pos.clone().add(half2)));
            } else {
                this.rSeccondaryGridBounds.assign(new Bounds3i(pos.clone(), pos.clone().add(size)));
            }

            this.rControl.reset();

            for (double x = this.rSeccondaryGridBounds.min.x; x < this.rSeccondaryGridBounds.max.x; x += offset.x) {
                for (double y = this.rSeccondaryGridBounds.min.y; y < this.rSeccondaryGridBounds.max.y; y += offset.y) {
                    for (double z = this.rSeccondaryGridBounds.min.z; z < this.rSeccondaryGridBounds.max.z; z += offset.z) {
                        assert context.bounds.contains(x, y, z);
                        assert rSeccondaryGridBounds.contains(x,y,z);

                        if (this.rControl.stop) return;

                        this.rPosition.assign(x, y, z);
                        context.pipe.accept(this.rPosition, this.rControl);
                    }
                }
            }
        }
    }
}
