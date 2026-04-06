package com.png.GridWaveCore.AlgoNodes.WFC;

import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.TileNodes.TileSet;

import java.util.Arrays;
import java.util.Map;

public record GridTile(TileSet.TileEntry tileEntry, Vector3i positionOffset, GridTileType type) {
    public void appendLines(StringBuilder[] builders, String pathKey, int width) {
        String[] k = Arrays.stream(tileEntry.getMainRuleSet().getDebug())
                .map(s -> (s == null || s.isEmpty()) ? "?" : s)
                .toArray(String[]::new);

        k[0] = padRight(k[0], width);
        k[1] = padCenter(k[1], width * 2);
        k[2] = padLeft(k[2], width);
        k[3] = padCenter(k[3], width * 2);

        char[] c = corners.get(type);
        int m = pathKey == null || pathKey.isEmpty() ? 0 :
                (k[0].equals(pathKey) ? 1 : 0) |
                        (k[1].equals(pathKey) ? 2 : 0) |
                        (k[2].equals(pathKey) ? 4 : 0) |
                        (k[3].equals(pathKey) ? 8 : 0);

        String rot = rotationNumbers[tileEntry.rot()];

        builders[0].append(c[0]).append(" ").append(k[1]).append(" ").append(c[1]).append(" ");
        builders[1].append(k[0]).append(" ").append(p[m]).append(rot).append(k[2]).append(" ");
        builders[2].append(c[2]).append(" ").append(k[3]).append(" ").append(c[3]).append(" ");
    }

    private static final String[] rotationNumbers = {"₀", "₁", "₂", "₃"};
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
}
