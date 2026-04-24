package com.png.GridWaveCore.Utils.MirrorNode.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import javax.annotation.Nonnull;

public class Bounds3iExtension {
    public static void mirrorBoundsAroundVoxel(@Nonnull Bounds3i bounds, @Nonnull Axis axis, @Nonnull Vector3i anchor) {
        if (bounds.isZeroVolume()) {
            return;
        }

        Vector3i min = new Vector3i(bounds.min);
        Vector3i max = new Vector3i(bounds.max);
        max.sub(Vector3iUtil.ALL_ONES);

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

        bounds.min.set(min);
        bounds.max.set(max);
        bounds.correct();
        bounds.max.add(Vector3iUtil.ALL_ONES);
    }
}
