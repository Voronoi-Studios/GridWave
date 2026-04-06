package com.png.GridWaveCore.AlgoNodes.WFC;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.png.GridWaveCore.AlgoNodes.GridWave;
import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DebugUtils {
    public static void sendDebugLog(List<GridTile> gridTiles, int grid, String pathKey, SeedBox seedBox) {
        GridWave.Winner winner = GridWave.winnerMap.get(seedBox.toString()).get(); //Maybe need to add checks
        String generatedString = generateString(gridTiles, pathKey);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final String message = "Generated " + gridTiles.size() + " tiles based on " + winner.toString() + " with grid: " + grid + " :\n" + generatedString;
        scheduler.schedule(() -> LoggerUtil.getLogger().info(message), 2, TimeUnit.SECONDS);
    }

    public static String generateString(List<GridTile> gridTiles, String pathKey) {
        if (gridTiles == null) return "null";
        var list = new ArrayList<>(gridTiles);
        list.removeIf(Objects::isNull);
        list.sort(Comparator.comparingInt((GridTile gt) -> gt.positionOffset().x).thenComparingInt(gt -> gt.positionOffset().z));
        int maxKeyLenght = getMaxKeyLength(list);
        StringBuilder sb = new StringBuilder();
        StringBuilder[] lines = {new StringBuilder(), new StringBuilder(), new StringBuilder()};

        int lastX = list.getFirst().positionOffset().x;

        for (GridTile gridTile : list) {
            if (lastX != gridTile.positionOffset().x) {
                sb.insert(0, lines[2].toString() + "\n");
                sb.insert(0, lines[1].toString() + "\n");
                sb.insert(0, lines[0].toString() + "\n");

                lines[0].setLength(0);
                lines[1].setLength(0);
                lines[2].setLength(0);

                lastX = gridTile.positionOffset().x;
            }
            gridTile.appendLines(lines, pathKey, maxKeyLenght);
        }

        sb.insert(0, lines[2].toString() + "\n");
        sb.insert(0, lines[1].toString() + "\n");
        sb.insert(0, lines[0].toString() + "\n");

        return sb.toString();
    }

    private static int getMaxKeyLength(List<GridTile> list) {
        if (list == null) return 1;
        return list.stream()
                .filter(tile -> tile != null && tile.tileEntry() != null && tile.tileEntry().ruleSets() != null)
                .flatMap(tile -> tile.tileEntry().ruleSets().values().stream())
                .filter(Objects::nonNull)
                .map(RuleSet.Combo::getDebug)
                .flatMap(Arrays::stream)
                .mapToInt(String::length)
                .max().orElse(1);
    }
}
