package ch.voronoi.GridWave.AlgoNodes.Helper;

public class AttemptBehavior {
    public int maxAttempts ;
    public int maxBacktracks;
    public int maxCollapsedCount;

    public AttemptBehavior(int maxAttempts, int maxBacktracks, int maxCollapsedCount){
        this.maxAttempts = maxAttempts;
        this.maxBacktracks = maxBacktracks;
        this.maxCollapsedCount = maxCollapsedCount;
    }
}

