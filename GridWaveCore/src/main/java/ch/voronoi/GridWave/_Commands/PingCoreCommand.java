package ch.voronoi.GridWave._Commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

import javax.annotation.Nonnull;

public class PingCoreCommand extends CommandBase {
    private final String pluginName;
    private final String pluginVersion;

    public PingCoreCommand(String pluginName, String pluginVersion) {
        super("ping", "Prints a test message from the " + pluginName + " plugin.");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER); // Allows the command to be used by anyone, not just OP
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("Hello from the " + pluginName + " v" + pluginVersion + " plugin!"));

    }
}
