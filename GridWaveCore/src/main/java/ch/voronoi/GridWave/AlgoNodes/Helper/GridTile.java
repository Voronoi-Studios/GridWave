package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.FeatureNodes.OverlapTileFeatureAsset;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.MirrorNode.StaticMirrorProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.OffsetProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.StaticRotatorProp;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static ch.voronoi.GridWave.TileSetNodes.TileSet.TileEntry.toRotation;

public record GridTile(TileSet.TileEntry tileEntry, Vector3ic actualPosition, GridTileType type, LinkedHashSet<POIInfo> connectedPOIs) {
    public void appendLines(StringBuilder[] builders, List<String> pathKeys, int width) {
        String[] k = Arrays.stream(tileEntry.getMainRuleSet().toHorizontalStringArray())
                .map(s -> (s == null || s.isEmpty()) ? "?" : s)
                .toArray(String[]::new);

        k[0] = padRight(k[0], width);
        k[1] = padCenter(k[1], width * 2);
        k[2] = padLeft(k[2], width);
        k[3] = padCenter(k[3], width * 2);

        char[] c = corners.get(type);
        int m = pathKeys == null || pathKeys.isEmpty() ? 0 :
                (pathKeys.contains(k[0]) ? 1 : 0) |
                (pathKeys.contains(k[1]) ? 2 : 0) |
                (pathKeys.contains(k[2]) ? 4 : 0) |
                (pathKeys.contains(k[3]) ? 8 : 0);

        String rot = subscripts[tileEntry.rot()];
        String pois = subscripts[connectedPOIs.size()];

        builders[0].append(c[0]).append(" ").append(k[1]).append(" ").append(c[1]).append(" ");
        builders[1].append(k[0]).append(pois).append(p[m]).append(rot).append(k[2]).append(" ");
        builders[2].append(c[2]).append(" ").append(k[3]).append(" ").append(c[3]).append(" ");
    }

    private static final String[] subscripts = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};
    private static final Map<GridTileType, char[]> corners = Map.of(
            GridTileType.BASIC, new char[]{'┌', '┐', '└', '┘'},
            GridTileType.POI, new char[]{'╔', '╗', '╚', '╝'},
            GridTileType.FANCY, new char[]{'┏', '┓', '┗', '┛'}
    );
    private static final char[] p = {
            ' ',   // 0b0000 = no connections
            '←',   // 0b0001 = W
            '↑',   // 0b0010 = N
            '┘',   // 0b0011 = N+W
            '→',   // 0b0100 = E
            '─',   // 0b0101 = E+W
            '└',   // 0b0110 = N+E
            '┴',   // 0b0111 = N+E+W
            '↓',   // 0b1000 = S
            '┐',   // 0b1001 = S+W
            '│',   // 0b1010 = N+S
            '┤',   // 0b1011 = N+S+W
            '┌',   // 0b1100 = E+S
            '┬',   // 0b1101 = N+E+S
            '├',   // 0b1110 = E+S+W
            '┼'    // 0b1111 = N+E+S+W
    };

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s;
        return " ".repeat(width - s.length()) + s;
    }

    private static String padCenter(String s, int width) {
        if (s.length() >= width) return s;
        int total = width - s.length();
        int left = total / 2;
        int right = total - left - 1;
        return " ".repeat(left) + s + " ".repeat(right);
    }

    public Function<TileSetAsset.Argument, Prop> getFullPropFunction(){
        return argument -> {
            TileSet.TileEntry entry = new TileSet.TileEntry(tileEntry());
            if (entry.propFunction() == null) return EmptyProp.INSTANCE;
            TileSetAsset.Argument subArgument = new TileSetAsset.Argument(argument); //Might be needed, to stop some cross-referencing
            Prop prop = entry.propFunction().apply(subArgument);
            Prop rotatedProp = new StaticRotatorProp(prop, RotationTuple.of(toRotation(entry.rot()), Rotation.None, Rotation.None), subArgument.materialCache);
            Prop mirroredProp = entry.mirrorDirection().toAxis() == null ? rotatedProp : new StaticMirrorProp(rotatedProp, entry.mirrorDirection().toAxis(), subArgument.materialCache);
            AtomicReference<Prop> propReference = new AtomicReference<>(new OffsetProp(getOffset(argument, entry),mirroredProp));
            argument.algoAsset.getFeatureAssets().forEach(feature -> feature.AfterPropCreation(propReference, entry, argument));
            entry.getFeatureAssets().forEach(feature -> feature.AfterPropCreation(propReference, entry, argument));
            return propReference.get();
        };
    }

    //Is probably not correctly doing Tiles which have an actualPosition that differs
    private static Vector3i getOffset(TileSetAsset.Argument argument, TileSet.TileEntry entry) {
        boolean localSwap = entry.hasFeature(OverlapTileFeatureAsset.class);
        boolean globalSwap = argument.algoAsset.hasFeature(OverlapTileFeatureAsset.class);
        Vector3ic grid = argument.algoAsset.getGrid();
        int evenOffsetX = (grid.x() % 2 == 0) ? 1 : 0;
        int evenOffsetZ = (grid.z() % 2 == 0) ? 1 : 0;
        if (globalSwap || localSwap){
            evenOffsetX = 1 - evenOffsetX;
            evenOffsetZ = 1 - evenOffsetZ;
        }

        Vector3ic anchorOffset = switch (entry.rot()){ //To-Do: Check if this  is actually offsetting correctly for nonuniform grids
            case 3 -> new Vector3i(evenOffsetX, 0, 0);
            case 2 -> new Vector3i(evenOffsetX, 0, evenOffsetZ);
            case 1 -> new Vector3i(0, 0, evenOffsetZ);
            default -> new Vector3i(0, 0, 0);
        };
        return new Vector3i(entry.getMultiTileOffset()).add(anchorOffset);
    }
}
