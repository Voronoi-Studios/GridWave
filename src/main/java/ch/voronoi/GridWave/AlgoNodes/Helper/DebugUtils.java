package ch.voronoi.GridWave.AlgoNodes.Helper;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import ch.voronoi.GridWave.AlgoNodes.GridWave;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.FeatureNodes.PathKeyAsset;
import ch.voronoi.GridWave.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DebugUtils {
    public static void sendDebugLog(List<GridTile> gridTiles, int grid, List<FeatureAsset> featureAssets, SeedBox seedBox) {
        GridWave.Winner winner = GridWave.winnerMap.get(seedBox.toString()).get(); //Maybe need to add checks
        List<String> pathKeys = featureAssets.stream().filter(PathKeyAsset.class::isInstance).map(PathKeyAsset.class::cast).flatMap(a -> Arrays.stream(a.getPathKeys())).toList();
        String generatedString = generateString(gridTiles, pathKeys);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        String str ="Generated " + gridTiles.size() + " tiles with grid: " + grid;
        if(winner != null) str += ", based on " + winner;
        String finalStr = str;
        scheduler.schedule(() -> {
            LoggerUtil.getLogger().info("\n" + generatedString + "\n\n" + finalStr + "\n\n"); scheduler.shutdown();
            Message message = Message.raw(finalStr).color(Color.GRAY);
            sendNotification(message, winner.success() ?"Icons/AssetNotifications/IconCheckmark.png" : "Icons/AssetNotifications/IconAlert.png");
        }, 2, TimeUnit.SECONDS);
    }

    public static void sendNotification(Message message1, String icon) {
        PermissionsModule perms = PermissionsModule.get();
        List<PlayerRef> playerRefs = Universe.get().getPlayers().stream().filter(playerRef -> perms.getGroupsForUser(playerRef.getUuid()).contains("OP")).toList();
        playerRefs.forEach( playerRef -> NotificationUtil.sendNotification(playerRef.getPacketHandler(), message1,icon));
    }

    public static String generateString(@Nonnull List<GridTile> gridTiles, List<String> pathKeys) {
        if (gridTiles.stream().allMatch(Objects::isNull)) return "Failed, everything is empty";
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
            gridTile.appendLines(lines, pathKeys, maxKeyLenght);
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
