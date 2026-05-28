package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3iUtil;

public class MaxBounds extends CustomBoundsAsset {
    public static final MaxBounds INSTANCE = new MaxBounds();

    @Override
    public Bounds3i build(){
        return new Bounds3i(Vector3iUtil.MIN, Vector3iUtil.MAX);
    }
}
