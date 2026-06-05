package ch.voronoi.GridWave._Commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;


import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

public class PatchNodeEditorCommand extends CommandBase {
    private final Path sourceDirectory;
    private final DefaultArg<String> patchTarget;

    public PatchNodeEditorCommand(Path sourceDirectory, Path patchTarget) {
        super("patch", "Adds the custom nodes to your local NodeEditor installation");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADMIN);
        this.sourceDirectory = sourceDirectory;
        this.patchTarget = this.withDefaultArg("patchTarget","path to `NodeEditor/Workspaces/HytaleGenerator Java`", ArgTypes.STRING, patchTarget == null ? null : patchTarget.toAbsolutePath().normalize().toString(),"what I think it is");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        Path target = Paths.get(patchTarget.get(ctx));

        if (!Files.isDirectory(target)) {
            ctx.sendMessage(Message.raw("Target directory does not exist: " + target));
            return;
        }

        try {
            // Backup _Workspace.json -> _Workspace_org.json if backup doesn't exist yet
            Path workspace = target.resolve("_Workspace.json");
            Path workspaceBackup = target.resolve("_Workspace_orig.json");
            if (Files.exists(workspace) && Files.exists(workspaceBackup)) {
                ctx.sendMessage(Message.raw("Already patched! No changes where made."));
                return;
            }
            Files.copy(workspace, workspaceBackup);

            String subPath = "Client/NodeEditor/Workspaces/HytaleGenerator Java";
            boolean isJar = sourceDirectory.toString().endsWith(".jar");

            FileSystem jarFs = null;
            Path sourcePath;
            if (isJar) {
                jarFs = FileSystems.newFileSystem(URI.create("jar:" + sourceDirectory.toUri()), Map.of());
                sourcePath = jarFs.getPath(subPath);
            } else {
                sourcePath = sourceDirectory.resolve(subPath);
            }

            // Walk sourcePath and copy everything to target
            try (Stream<Path> stream = Files.walk(sourcePath)) {
                stream.forEach(source -> {
                    Path dest = target.resolve(sourcePath.relativize(source).toString());
                    try {
                        if (Files.isDirectory(source)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } finally {
                if (jarFs != null) jarFs.close();
            }

            ctx.sendMessage(Message.raw("Patched the NodeEditor successfully at:\n" + target));
        } catch (IOException | UncheckedIOException e) {
            ctx.sendMessage(Message.raw("Patch failed: " + e.getMessage()));
        }
    }
}
