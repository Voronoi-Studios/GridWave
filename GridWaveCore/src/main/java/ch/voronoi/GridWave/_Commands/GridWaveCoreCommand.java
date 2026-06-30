package ch.voronoi.GridWave._Commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class GridWaveCoreCommand extends AbstractCommandCollection {
        public GridWaveCoreCommand(String pluginName, String pluginVersion, Path patchSource, @Nullable Path patchTarget) {
            super("GridWave.core", "Commands related to GridWave.core");
            this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
            this.addSubCommand(new PingCoreCommand(pluginName,pluginVersion));
            this.addSubCommand(new GenerateCommand());
            this.addSubCommand(new GenerateTileCommand());
            this.addSubCommand(new PatchNodeEditorCommand(patchSource,patchTarget));
        }

}
