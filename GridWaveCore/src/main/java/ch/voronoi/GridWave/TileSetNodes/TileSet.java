package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.HorizontalRules;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleSet;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.Utils.MirrorNode.Helper.MirrorDirection;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static ch.voronoi.GridWave.AlgoNodes.Helper.DebugUtils.VectorStr;

public class TileSet {
    protected final List<TileEntry> tileEntries;
    protected final List<FeatureAsset> tileFeatureAssets;
    protected final Function<TileSetAsset.Argument, Prop> basePropFunction;

    public TileSet(@Nonnull Map<Vector3ic, RuleCombo> ruleSets, double weight, boolean minimizeVariants, Function<TileSetAsset.Argument, Prop> basePropFunction, TileSetAsset.Argument argument, @Nonnull List<FeatureAsset> tileFeatureAssets) {
        this.tileEntries = new ArrayList<>();
        this.tileFeatureAssets = tileFeatureAssets;
        this.basePropFunction = basePropFunction;
        Set<String> seen = new HashSet<>();
        for (int rotation = 0; rotation < 4; rotation++) {
            Map<Vector3ic, RuleCombo> current = new HashMap<>();
            for (Map.Entry<Vector3ic, RuleCombo> e : ruleSets.entrySet()) {
                Vector3i rotatedKey = rotate(e.getKey(), rotation);
                RuleCombo rotatedValue = rotate(e.getValue(), rotation);
                current.put(rotatedKey, rotatedValue);
            }
            TileEntry tileEntry = new TileEntry(current, new Vector3i(Vector3iUtil.ZERO), weight, rotation, MirrorDirection.None, basePropFunction, null, new ArrayList<>(tileFeatureAssets));
            if (!minimizeVariants || seen.add(getKey(current))) tileEntries.add(tileEntry);

        }
        for (MirrorDirection mirrorDirection : List.of(MirrorDirection.X, MirrorDirection.Z)) {
            Map<Vector3ic, RuleCombo> current = new HashMap<>();
            for (Map.Entry<Vector3ic, RuleCombo> e : ruleSets.entrySet()) {
                Vector3i mirroredKey = mirror(e.getKey(), mirrorDirection);
                RuleCombo mirroredValue = mirror(e.getValue(), mirrorDirection);
                current.put(mirroredKey, mirroredValue);
            }
            TileEntry tileEntry = new TileEntry(current, new Vector3i(Vector3iUtil.ZERO), weight, 0, mirrorDirection, basePropFunction, null, new ArrayList<>(tileFeatureAssets));
            if (!minimizeVariants || seen.add(getKey(current))) tileEntries.add(tileEntry);
        }
        tileFeatureAssets.forEach(feature -> feature.AfterTileSetCreation(tileEntries, argument));
    }
    private String getKey(Map<Vector3ic, RuleCombo> current) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Vector3ic, RuleCombo> entry : current.entrySet()) {
            sb.append(VectorStr(entry.getKey())).append("-").append(entry.getValue().toString());
        }
        return sb.toString();
    }

    /** This includes all rotations / mirrors of the same tile */
    public @Nonnull Stream<TileEntry> getTileEntries() { return tileEntries.stream();}
    public @Nonnull Stream<FeatureAsset> getTileFeatureAssets() { return tileFeatureAssets.stream(); }
    public Function<TileSetAsset.Argument, Prop> getBasePropFunction() { return basePropFunction; }

    private static RuleCombo rotate(RuleCombo ruleCombo, int r) {
        String[][] horizontalProviderArr = ruleCombo.providerRuleSet().horizontalRules().getArrays();
        String[][] horizontalReceiverArr = ruleCombo.recieverRuleSet().horizontalRules().getArrays();
        return new RuleCombo(
                new RuleSet(
                        new HorizontalRules(rotate(horizontalProviderArr, r)), ruleCombo.providerRuleSet().verticalRules()),
                new RuleSet(
                        new HorizontalRules(rotate(horizontalReceiverArr, r)), ruleCombo.recieverRuleSet().verticalRules()),
                        ruleCombo.elevationRules());
    }

    private static String[][] rotate(String[][] arr, int r) {
        int l = 4;
        String[][] rotated = new String[l][];
        for (int i = 0; i < l; i++) {
            rotated[i] = arr[(i + r) % l];
        }
        return rotated;
    }

    private static Vector3i rotate(Vector3ic v, int r) {
        return switch (r & 3) {
            case 1 -> new Vector3i(v.z(), v.y(), -v.x());
            case 2 -> new Vector3i(-v.x(), v.y(), -v.z());
            case 3 -> new Vector3i(-v.z(), v.y(), v.x());
            default -> new Vector3i(v);
        };
    }

    private static RuleCombo mirror(RuleCombo ruleCombo, MirrorDirection m) {
        String[][] horizontalProviderArr = ruleCombo.providerRuleSet().horizontalRules().getArrays();
        String[][] horizontalReceiverArr = ruleCombo.recieverRuleSet().horizontalRules().getArrays();
        return new RuleCombo(
                new RuleSet(
                        new HorizontalRules(mirror(horizontalProviderArr, m)), ruleCombo.providerRuleSet().verticalRules()),
                new RuleSet(
                        new HorizontalRules(mirror(horizontalReceiverArr, m)), ruleCombo.recieverRuleSet().verticalRules()),
                ruleCombo.elevationRules());
    }

    private static String[][] mirror(String[][] arr, MirrorDirection m) {
        int l = 4;
        String[][] mirrored = new String[l][];
        for (int i = 0; i < l; i++) {
            if(m == MirrorDirection.Z && i%2 == 0) mirrored[i] = arr[(i + 2) % l];
            else if(m == MirrorDirection.X && i%2 == 1) mirrored[i] = arr[(i + 2) % l];
            else mirrored[i] = arr[i];
        }
        return mirrored;
    }

    private static Vector3i mirror(Vector3ic v, MirrorDirection m) {
        return switch (m) {
            case MirrorDirection.X -> new Vector3i(-v.x(), v.y(), v.z());
            case MirrorDirection.Z -> new Vector3i(v.x(), v.y(), -v.z());
            default -> new Vector3i(v);
        };
    }
}
