package ch.voronoi.GridWave;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;


import javax.annotation.Nonnull;

public class GenerateCommand extends AbstractPlayerCommand {
    RequiredArg<String> propName;

    public GenerateCommand() {
        super("generate", "generates prop Node");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        propName = this.withRequiredArg("PropName", "the export name of the prop NODE", ArgTypes.STRING);
    }


    @Override
    protected void execute(@NonNull CommandContext ctx, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PropAsset.Argument propArgument = new PropAsset.Argument(new SeedBox("command"), new MaterialCache(), new ReferenceBundle(), WorkerIndexer.Id.MAIN);
        PropAsset propAsset = PropAsset.getExportedAsset(propName.get(ctx));
        if (propAsset == null) { ctx.sendMessage(Message.raw("This Prop Node does not exist")); return; }
        BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.get().getState(player, playerRef);
        BlockSelection blockSelection = createSelectionFromPropAsset(propAsset, propArgument);
        builderState.setSelection(blockSelection);
        builderState.sendSelectionToClient();
        //playerRef.getPacketHandler().write(((BlockSelection) java.util.Objects.requireNonNullElseGet(blockSelection, BlockSelection::new)).toPacket());
    }


    public BlockSelection createSelectionFromPropAsset(
            PropAsset propAsset,
            PropAsset.Argument propArgument
    ) {
        Prop prop = propAsset.build(propArgument);

        Bounds3i readBounds = prop.getReadBounds_voxelGrid().clone();
        Bounds3i writeBounds = prop.getWriteBounds_voxelGrid().clone();

        writeBounds.max.y += 50;
//        readBounds.correct();
//        writeBounds.correct();

        Bounds3i bounds = writeBounds.clone();
        //bounds.max.y += 50; //Hacky lol

        ArrayVoxelSpace<Material> readSpace = new ArrayVoxelSpace<>(readBounds);
        ArrayVoxelSpace<Material> writeSpace = new ArrayVoxelSpace<>(writeBounds);

        Prop.Context context = new Prop.Context(
                new Vector3i(0, 0, 0),
                readSpace,
                writeSpace,
                EntityFunnel.NULL,
                0.0
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

        Vector3i localMin = Vector3i.ZERO;
        Vector3i localMax = writeBounds.getSize();

        selection.setSelectionArea(localMin, localMax);
        selection.setAnchor(
                (localMin.x + localMax.x) / 2,
                (localMin.y + localMax.y) / 2,
                (localMin.z + localMax.z) / 2
        );

        return selection;
    }
}