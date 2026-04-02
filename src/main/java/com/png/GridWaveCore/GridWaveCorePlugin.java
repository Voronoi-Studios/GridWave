package com.png.GridWaveCore;

import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.png.GridWaveCore.AlgoNodes.*;
import com.png.GridWaveCore.SeedNodes.*;
import com.png.GridWaveCore.TileNodes.*;
import com.png.GridWaveCore.UnusedNodes.*;

public class GridWaveCorePlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public GridWaveCorePlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        //Test stuff
        this.getCommandRegistry().registerCommand(new PingCommand(this.getName(), this.getManifest().getVersion().toString()));
        PropAsset.CODEC.register("CPrefab", CPrefabPropAsset.class, CPrefabPropAsset.CODEC);

        //Algo Nodes
        PropAsset.CODEC.register("Algo", AlgoAsset.class, AlgoAsset.CODEC);
        //RuleSetAsset -> AssetBuilderCodec

        //Tile Nodes
        TileSetAsset.CODEC.register("Single", SingleTileSetAsset.class, SingleTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Multi", MultiTileSetAsset.class, MultiTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Fixed", FixedTileSetAsset.class, FixedTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Imported", ImportedTileSetAsset.class, ImportedTileSetAsset.CODEC);

        //Seed Nodes
        SeedAsset.CODEC.register("Constant", ConstantSeedAsset.class, ConstantSeedAsset.CODEC);
        SeedAsset.CODEC.register("Random", RandomSeedAsset.class, RandomSeedAsset.CODEC);
        SeedAsset.CODEC.register("Time", TimeSeedAsset.class, TimeSeedAsset.CODEC);
        SeedAsset.CODEC.register("Imported", ImportedSeedAsset.class, ImportedSeedAsset.CODEC);
    }

    public static class RotationValidator implements Validator<Integer> {

        @Override
        public void accept(Integer v, ValidationResults r) {
            if (v != 0 && v != 90 && v != 180 && v != 270) {
                r.fail("Rotation can only have the values: 0, 90, 180, 270");
            }
        }

        @Override
        public void updateSchema(SchemaContext context, Schema schema) {}
    }
}
