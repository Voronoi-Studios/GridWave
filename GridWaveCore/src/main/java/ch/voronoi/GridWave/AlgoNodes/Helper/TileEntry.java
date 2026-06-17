package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.FeatureNodes.OverlapTileFeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.MirrorNode.Helper.MirrorDirection;
import ch.voronoi.GridWave.Utils.MirrorNode.StaticMirrorProp;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.OffsetProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public record TileEntry (Map<Vector3ic, RuleCombo> ruleSets, Vector3ic mainKey,
                         double weight, int rot, MirrorDirection mirrorDirection,
                         Function<TileSetAsset.Argument, Prop> basePropFunction,
                         Vector3ic restrained, List<FeatureAsset> tileFeatures) implements IFeatureCheck {

    public TileEntry(SectionData.Entry entry) {
        this(
                Map.of(Vector3iUtil.ZERO, entry.ruleSet), Vector3iUtil.ZERO,
                1, 0, MirrorDirection.None, null, null, new ArrayList<>()
        );
    }
    public TileEntry(RuleCombo ruleCombo) {
        this(Map.of(new Vector3i(Vector3iUtil.ZERO), ruleCombo), new Vector3i(Vector3iUtil.ZERO), 1, 0, MirrorDirection.None, null, null, new ArrayList<>());
    }

    public RuleCombo getMainRuleSet() {
        return ruleSets.get(mainKey);
    }
    public double getWeight(Map<Vector3ic, WaveCell> wave, TileSetAsset.Argument argument) {
        AtomicReference<Double> newWeight = new AtomicReference<>(weight);
        tileFeatures.forEach(feature -> feature.ReplaceWeight(newWeight, this, wave, argument));
        return newWeight.get();
    }

    @Override public List<FeatureAsset> getFeatureAssets() {
        return tileFeatures;
    }

    public List<TileEntry> getSubTiles() {
        var result = new ArrayList<TileEntry>();
        for (Vector3ic subIdentifier : ruleSets.keySet()) {
            result.add(new TileEntry(
                    new HashMap<>(ruleSets),
                    new Vector3i(subIdentifier),
                    weight,
                    rot,
                    mirrorDirection,
                    subIdentifier.equals(mainKey) ? basePropFunction : null, //null if not corner
                    restrained,
                    new ArrayList<>(tileFeatures))
            );
        }
        return result;
    }



    public Function<TileSetAsset.Argument, Prop> getEntryPropFunction() {
        return argument -> {
            if (basePropFunction == null) return EmptyProp.INSTANCE;
            TileSetAsset.Argument subArgument = new TileSetAsset.Argument(argument); //Might be needed, to stop some cross-referencing
            Prop prop = basePropFunction.apply(subArgument);
            Prop rotatedProp = new StaticRotatorProp(prop, getRotationTuple(rot), subArgument.materialCache);
            Prop mirroredProp = mirrorDirection.toAxis() == null ? rotatedProp : new StaticMirrorProp(rotatedProp, mirrorDirection.toAxis(), subArgument.materialCache);
            return new OffsetProp(new Vector3i(getPropAnchorOffset(argument)).add(getPropMultiTileOffset()), mirroredProp);
        };
    }
    public static RotationTuple getRotationTuple(int rot) {
        return RotationTuple.of( switch (rot) {
            case 1 -> Rotation.Ninety;
            case 2 -> Rotation.OneEighty;
            case 3 -> Rotation.TwoSeventy;
            default -> Rotation.None;
        },Rotation.None, Rotation.None);
    }


    private Vector3i getPropAnchorOffset(TileSetAsset.Argument argument){
        Vector3ic grid = argument.algoAsset.getGrid();
        int evenOffsetX = (grid.x() % 2 == 0) ? 1 : 0;
        int evenOffsetZ = (grid.z() % 2 == 0) ? 1 : 0;

        Bounds3i bounds = new Bounds3i();
        ruleSets.keySet().forEach(bounds::encompass);
        Vector3ic signedSize = new Vector3i(bounds.min.x + bounds.max.x - 1,bounds.min.y + bounds.max.y - 1,bounds.min.z + bounds.max.z - 1);
        if (signedSize.x() != 0 && signedSize.x() % 2 != 0) evenOffsetX = 1 - evenOffsetX;
        if (signedSize.z() != 0 && signedSize.z() % 2 != 0) evenOffsetZ = 1 - evenOffsetZ;

        boolean localSwap = hasFeature(OverlapTileFeatureAsset.class);
        boolean globalSwap = argument.algoAsset.hasFeature(OverlapTileFeatureAsset.class);
        if (globalSwap || localSwap){
            evenOffsetX = 1 - evenOffsetX;
            evenOffsetZ = 1 - evenOffsetZ;
        }

        return switch (mirrorDirection()){
            case MirrorDirection.X -> new Vector3i(evenOffsetX, 0, 0);
            case MirrorDirection.Z -> new Vector3i(0, 0, evenOffsetZ);
            default -> switch (rot()) {
                case 3 -> new Vector3i(0, 0, evenOffsetZ);
                case 2 -> new Vector3i(evenOffsetX, 0, evenOffsetZ);
                case 1 -> new Vector3i(evenOffsetX, 0, 0);
                default -> new Vector3i(0, 0, 0);
            };
        };
    }

    private Vector3ic getPropMultiTileOffset() {
        if (ruleSets.isEmpty() || ruleSets.size() == 1) return new Vector3i(0, 0, 0);

        Bounds3i bounds = new Bounds3i();
        ruleSets.keySet().forEach(bounds::encompass);
        Vector3ic signedSize = new Vector3i(bounds.min.x + bounds.max.x - 1,bounds.min.y + bounds.max.y - 1,bounds.min.z + bounds.max.z - 1);

        return new Vector3i(
                (int)Math.floor(signedSize.x() / 2d),
                signedSize.y(),
                (int)Math.floor(signedSize.z() / 2d)
        );
    }

    public Vector3ic getPoiKey() {return getPropMultiTileOffset(); }

    public TileEntry restrain(Vector3ic absolutePos){
        return new TileEntry(ruleSets, mainKey, weight, rot,
            mirrorDirection, basePropFunction,
                absolutePos, tileFeatures);
    }
}
