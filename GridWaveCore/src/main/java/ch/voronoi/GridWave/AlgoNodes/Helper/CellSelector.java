package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.Helper.EarlyExitReason;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import java.util.*;

public abstract class CellSelector {
    public abstract CellSelectorResult select(
            Map<Vector3ic, WaveCell> wave,
            Stack<List<WaveCellChange>> undoQue,
            AttemptBehavior attemptBehavior,
            int backtracksCount,
            Random random
    );

    public static @NonNull CellSelectorResult Backtrack(Stack<List<WaveCellChange>> undoQue, Map<Vector3ic, WaveCell> wave) {
        if(!undoQue.isEmpty()){
            List<WaveCellChange> changes = undoQue.pop();
            if(changes != null){
                for (WaveCellChange change : changes) {
                    if (change.cell() != null) wave.put(change.pos(), change.cell());
                }
            }
        }
        return new CellSelectorResult(null, EarlyExitReason.BACKTRACKED);
    }
}
