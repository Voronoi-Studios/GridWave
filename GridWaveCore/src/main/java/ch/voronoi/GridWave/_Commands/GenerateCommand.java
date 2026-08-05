package ch.voronoi.GridWave._Commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.hytalegenerator.assets.ThreadBridge;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

public class GenerateCommand extends AbstractPlayerCommand {
    RequiredArg<String> propName;

    public GenerateCommand() {
        super("generate", "generates prop Node");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER); // Allows the command to be used by anyone, not just OP
        propName = this.withRequiredArg("PropName", "the export name of the prop NODE", ArgTypes.STRING);
    }


    @Override
    protected void execute(@NonNull CommandContext ctx, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PropAsset.Argument propArgument = new PropAsset.Argument(new SeedBox("command"), new MaterialCache(), new ReferenceBundle(), WorkerIndexer.Id.MAIN, new ThreadBridge());
        PropAsset propAsset = PropAsset.getExportedAsset(propName.get(ctx));
        if (propAsset == null) {
            ctx.sendMessage(Message.raw("This Prop Node does not exist"));
        }
        else {
            BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(player, playerRef);
            Prop prop = propAsset.build(propArgument);
            BlockSelection blockSelection = createSelectionFromPropAsset(prop, propArgument);
            builderState.setSelection(blockSelection);
            builderState.sendSelectionToClient();
        }
    }


    public static BlockSelection createSelectionFromPropAsset(
            Prop prop,
            PropAsset.Argument propArgument
    ) {
        Bounds3i readBounds = prop.getReadBounds_voxelGrid().clone();
        Bounds3i writeBounds = prop.getWriteBounds_voxelGrid().clone();
        Bounds3i bounds = writeBounds.clone();
        ArrayVoxelSpace<Material> readSpace = new ArrayVoxelSpace<>(readBounds);
        ArrayVoxelSpace<Material> writeSpace = new ArrayVoxelSpace<>(writeBounds);

        Prop.Context context = new Prop.Context(
                new Vector3i(0, 0, 0),
                readSpace,
                writeSpace,
                EntityFunnel.NULL,
                0.0,
                null,
                null
        );

        prop.generate(context);

        BlockSelection selection = new BlockSelection();
        MaterialCache materialCache = propArgument.materialCache;

        for (int x = bounds.min.x; x < bounds.max.x; x++) {
            for (int y = bounds.min.y; y < bounds.max.y; y++) {
                for (int z = bounds.min.z; z < bounds.max.z; z++) {
                    Material material = writeSpace.get(x, y, z);
                    if (material == null || material.equals(materialCache.EMPTY)) {
                        continue;
                    }

                    int localX = x - bounds.min.x;
                    int localY = y - bounds.min.y;
                    int localZ = z - bounds.min.z;

                    SolidMaterial solid = material.solid();
                    FluidMaterial fluid = material.fluid();

                    if (solid.blockId != 0) {
                        selection.addBlockAtLocalPos(
                                localX,
                                localY,
                                localZ,
                                solid.blockId,
                                solid.rotation,
                                solid.filler,
                                solid.support,
                                solid.holder
                        );
                    }

                    if (fluid.fluidId != 0) {
                        selection.addFluidAtLocalPos(
                                localX,
                                localY,
                                localZ,
                                fluid.fluidId,
                                fluid.fluidLevel
                        );
                    }
                }
            }
        }

        Vector3ic localMin = Vector3iUtil.ZERO;
        Vector3ic localMax = writeBounds.getSize();
        selection.setSelectionArea(localMin, localMax);
        selection.setAnchor(
                (localMin.x() + localMax.x()) / 2,
                (localMin.y() + localMax.y()) / 2,
                (localMin.z() + localMax.z()) / 2
        );
        return selection;
    }
}