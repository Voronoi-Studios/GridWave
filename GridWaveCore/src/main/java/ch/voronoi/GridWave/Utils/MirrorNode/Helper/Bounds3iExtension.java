package ch.voronoi.GridWave.Utils.MirrorNode.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class Bounds3iExtension {
    public static void mirrorBoundsAroundVoxel(@Nonnull Bounds3i bounds, @Nonnull Axis axis, @Nonnull Vector3i anchor) {
        if (bounds.isZeroVolume()) {
            return;
        }

        Vector3i min = bounds.min.clone();
        Vector3i max = bounds.max.clone();
        max.subtract(Vector3i.ALL_ONES);

        int mirroredMin;
        int mirroredMax;
        switch (axis) {
            case X:
                mirroredMin = anchor.x - (max.x - anchor.x);
                mirroredMax = anchor.x - (min.x - anchor.x);
                min.x = mirroredMin;
                max.x = mirroredMax;
                break;
            case Y:
                mirroredMin = anchor.y - (max.y - anchor.y);
                mirroredMax = anchor.y - (min.y - anchor.y);
                min.y = mirroredMin;
                max.y = mirroredMax;
                break;
            case Z:
                mirroredMin = anchor.z - (max.z - anchor.z);
                mirroredMax = anchor.z - (min.z - anchor.z);
                min.z = mirroredMin;
                max.z = mirroredMax;
                break;
            default:
                throw new IllegalStateException("Unexpected axis: " + axis);
        }

        bounds.min.assign(min);
        bounds.max.assign(max);
        bounds.correct();
        bounds.max.add(Vector3i.ALL_ONES);
    }
}
