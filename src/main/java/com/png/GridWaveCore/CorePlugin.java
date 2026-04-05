package com.png.GridWaveCore;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.png.GridWaveCore.AlgoNodes.*;
import com.png.GridWaveCore.MirrorNode.StaticMirrorPropAsset;
import com.png.GridWaveCore.SeedNodes.*;
import com.png.GridWaveCore.TileNodes.*;
import com.png.GridWaveCore.UnusedNodes.*;

public class CorePlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CorePlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        //Test stuff
        this.getCommandRegistry().registerCommand(new PingCommand(this.getName(), this.getManifest().getVersion().toString()));
        PropAsset.CODEC.register("CPrefab", CPrefabPropAsset.class, CPrefabPropAsset.CODEC);
        PropAsset.CODEC.register("StaticMirror", StaticMirrorPropAsset.class, StaticMirrorPropAsset.CODEC);

        AssetRegistry.register(HytaleAssetStore.builder(TileSetAsset.class, new DefaultAssetMap<String, TileSetAsset>())
                .setPath("HytaleGenerator/TileSets")
                .setCodec(TileSetAsset.CODEC)
                .setKeyFunction(TileSetAsset::getId)
                .build()
        );

        //Algo Nodes
        PropDistributionAsset.CODEC.register("GridWave", GridWaveAsset.class, GridWaveAsset.CODEC);
        PropAsset.CODEC.register("GridWaveProp", GridWavePropAsset.class, GridWavePropAsset.CODEC);
        PositionProviderAsset.CODEC.register("GridGen2D", GridGen2DAsset.class, GridGen2DAsset.CODEC);
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
}
