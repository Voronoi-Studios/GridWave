package com.png.GridWaveCore.AlgoNodes.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

public class MapPropDistribution extends PropDistribution {
    private final Map<Vector3dc, Prop> positionToPropMap;

    public MapPropDistribution(Map<Vector3dc, Prop> positionToPropMap) {
        this.positionToPropMap = positionToPropMap;
    }

    @Override
    public void distribute(@Nonnull PropDistribution.Context context) {

        Control control = new Control();
        for (Map.Entry<Vector3dc, Prop> entry : this.positionToPropMap.entrySet()) {
            if (control.stop) break; // Respect pipeline stop signal
            context.pipe.accept(new Vector3d(entry.getKey()), entry.getValue(), control);
        }
    }

    @Override
    public void forEachPossibleProp(@Nonnull Consumer<Prop> consumer) {
        this.positionToPropMap.values().stream()
                .distinct()
                .forEach(consumer);
    }
}
