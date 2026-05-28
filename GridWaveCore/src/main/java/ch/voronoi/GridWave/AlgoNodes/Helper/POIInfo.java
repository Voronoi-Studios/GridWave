package ch.voronoi.GridWave.AlgoNodes.Helper;

import org.joml.Vector3ic;

public class POIInfo{
    public Vector3ic key;
    public int distance;

    public POIInfo(Vector3ic key) {
        this.key = key;
        this.distance = 0;
    }
}
