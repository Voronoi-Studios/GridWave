package ch.voronoi.GridWaveExamples._Commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

public class GridWaveExamplesCommand extends AbstractCommandCollection {
        public GridWaveExamplesCommand(String pluginName, String pluginVersion) {
            super("GridWave.examples", "Commands related to GridWave.examples");
            this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
            this.addSubCommand(new PingExamplesCommand(pluginName,pluginVersion));
        }

}
