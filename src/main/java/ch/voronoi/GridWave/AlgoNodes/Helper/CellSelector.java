package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.math.vector.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Random;

public abstract class CellSelector {
    public abstract CellSelectiorResult select(
            Map<Vector3i, WaveCell> wave,
            Deque<WaveCellChange> stack,
            AttemptBehavior attemptBehavior,
            int backtracksCount,
            Random random
    );

    public static @NonNull CellSelectiorResult Backtrack(Deque<WaveCellChange> stack, Map<Vector3i, WaveCell> wave) {
        for (int i = 0; i < 5 && !stack.isEmpty(); i++) {
            WaveCellChange change = stack.pop();
            if (change.cell() != null) wave.put(change.pos(), change.cell());
        }
        return new CellSelectiorResult(null, EarlyExitReason.BACKTRACKED);
    }
}
