package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.AlgoNodes.IAlgoAsset;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.plugin.Handle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
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

public class DebugUtils {
    public static void sendDebugLog(List<GridTile> gridTiles, TileSetAsset.Argument argument, GridWave.WFCResult wfcResult) {
        List<String> pathKeys = argument.algoAsset.getFeatureAssets().stream().filter(PathKeyAsset.class::isInstance).map(PathKeyAsset.class::cast).flatMap(a -> Arrays.stream(a.getPathKeys())).toList();
        String generatedString = generateString(gridTiles, pathKeys);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        String str ="Generated " + gridTiles.size() + " tiles with grid: " + VectorStr(argument.algoAsset.getGrid());
        String subStr = wfcResult.toString();
        sendNotification(Message.raw(str), Message.raw(subStr), "Icons/AssetNotifications/icon-256.png",  wfcResult.success ? NotificationStyle.Success : NotificationStyle.Warning);
        scheduler.schedule(() -> {
            LoggerUtil.getLogger().info("\n" + generatedString + "\n\n" + str + "\n" + subStr + "\n\n"); scheduler.shutdown();
        }, 2, TimeUnit.SECONDS);
    }

    public static void sendNotification(Message message1, Message message2, String icon, NotificationStyle style) {
        PermissionsModule perms = PermissionsModule.get();
        //It does not seem possible to get the world we are currently generating...
        List<PlayerRef> playerRefs = Universe.get().getPlayers().stream().filter(playerRef -> perms.getGroupsForUser(playerRef.getUuid()).contains("OP")).toList();
        playerRefs.forEach( playerRef -> NotificationUtil.sendNotification(playerRef.getPacketHandler(), message1, message2, icon, style));
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
                .map(RuleSet.Combo::toStringArray)
                .flatMap(Arrays::stream)
                .mapToInt(String::length)
                .max().orElse(1);
    }

    public static String VectorStr(Vector3i position) {
        return "[x"+position.x +", y"+position.y+", z"+position.z+"]";
    }
}
