package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.AssignedPropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3d;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

public class MapPropDistribution extends PropDistribution {
    private final Map<Vector3d, Prop> positionToPropMap;

    public MapPropDistribution(Map<Vector3d, Prop> positionToPropMap) {
        this.positionToPropMap = positionToPropMap;
    }

    @Override
    public void distribute(@Nonnull PropDistribution.Context context) {

        Control control = new Control();
        for (Map.Entry<Vector3d, Prop> entry : this.positionToPropMap.entrySet()) {
            if (control.stop) break; // Respect pipeline stop signal
            context.pipe.accept(entry.getKey(), entry.getValue(), control);
        }
    }

    @Override
    public void forEachPossibleProp(@Nonnull Consumer<Prop> consumer) {
        this.positionToPropMap.values().stream()
                .distinct()
                .forEach(consumer);
    }
}
