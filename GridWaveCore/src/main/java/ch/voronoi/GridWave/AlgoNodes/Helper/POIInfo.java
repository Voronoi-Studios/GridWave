package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.math.vector.Vector3i;

public class POIInfo{
    public Vector3i key;
    public int distance;

    public POIInfo(Vector3i key) {
        this.key = key;
        this.distance = 0;
    }
}
