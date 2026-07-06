package ch.voronoi.GridWave._Commands;

import ch.voronoi.GridWave.AlgoNodes.Helper.*;
import ch.voronoi.GridWave.AlgoNodes.PropAlgoAsset;
import ch.voronoi.GridWave.AlgoNodes.PropDistributionAlgoAsset;
import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.UnionProp;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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
import org.bson.BsonDateTime;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.NonNull;

import java.sql.Time;
import java.time.LocalTime;
import java.util.*;

import static ch.voronoi.GridWave.TileSetNodes.TileSetAsset.*;
import static ch.voronoi.GridWave._Commands.GenerateCommand.createSelectionFromPropAsset;

public class GenerateTileCommand extends AbstractPlayerCommand {
    RequiredArg<String> algoName;
    RequiredArg<String> size;
    RequiredArg<String> ruleSetString;

    public GenerateTileCommand() {
        super("generate-tile", "generates a (multi)Tile according to a StringRuleSet and a provided RuleSetGroupNode");
        this.setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER); // Allows the command to be used by anyone, not just OP
        this.algoName = this.withRequiredArg("algoName", "the export name of the algo NODE", ArgTypes.STRING);
        this.size = this.withRequiredArg("size", "the size of the desired tile in a singular string format {xSize}x{zSize}: 1x1, 5x2 ...", ArgTypes.STRING);
        this.ruleSetString = this.withRequiredArg("ruleSetString", "the rulesetString of the desired (multi)tile", ArgTypes.STRING);
    }


    @Override
    protected void execute(@NonNull CommandContext ctx, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        IAlgoAsset algoAsset = null;
        try{
            algoAsset = (IAlgoAsset)PropDistributionAlgoAsset.getExportedAsset(algoName.get(ctx));
        }catch (Exception _){}
        if(algoAsset == null){
            try{
                algoAsset = (IAlgoAsset)PropAlgoAsset.getExportedAsset(algoName.get(ctx));
            }catch (Exception _){}
        }

        if (algoAsset == null) { ctx.sendMessage(Message.raw("This Algo Node does not exist")); return; }
        var seedBox = new SeedBox(LocalTime.now().toString());
        Argument argument = new Argument(seedBox, new MaterialCache(), new ReferenceBundle(), WorkerIndexer.Id.MAIN, seedBox, new Bounds3i(), algoAsset);
        List<TileEntry> tileSets = algoAsset.getBaseTileSets(argument).stream().flatMap(TileSet::getTileEntries).toList();

        Player player = store.getComponent(ref, Player.getComponentType());
        RuleCombo[] simpleRuleSets = buildRuleCombo(ruleSetString.get(ctx),null);
        String[] sizeParts = size.get(ctx).split("x");
        Map<Vector3ic, RuleCombo> ruleSets = getRuleComboMap(algoAsset.getGrid(), new Vector3i(Integer.parseInt(sizeParts[0]), 0, Integer.parseInt(sizeParts[1])), simpleRuleSets);

        List<Prop> props = new ArrayList<>();

        for(var entry : ruleSets.entrySet()){
            if(entry.getValue().equals(RuleCombo.H_ALL_N)) continue;
            List<TileEntry> tileEntries = tileSets.stream().filter(x -> Match.fancyMatch(entry.getValue(), x.getMainRuleSet())).toList();
            if(tileEntries.isEmpty()) { ctx.sendMessage(Message.raw("There was no matching BaseTile found for " + entry.getValue())); return; }
            GridTile gridTile = new GridTile(tileEntries.get(Math.abs(argument.seedBox.createSupplier().get()) % tileEntries.size()), GridTileType.BASIC, entry.getKey(),entry.getKey(),new LinkedHashSet<>());
            props.add(gridTile.getFullPropFunction().apply(argument));
        }

        BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.get().getState(player, playerRef);
        BlockSelection blockSelection = createSelectionFromPropAsset(new UnionProp(props), argumentFrom(argument));
        builderState.setSelection(blockSelection);
        builderState.sendSelectionToClient();
        //playerRef.getPacketHandler().write(((BlockSelection) java.util.Objects.requireNonNullElseGet(blockSelection, BlockSelection::new)).toPacket());
    }

}