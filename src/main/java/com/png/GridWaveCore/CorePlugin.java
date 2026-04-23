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
import com.png.GridWaveCore.FeatureNodes.*;
import com.png.GridWaveCore.Utils.MirrorNode.StaticMirrorPropAsset;
import com.png.GridWaveCore.RuleSetNodes.AdvancedRuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.RuleSetAsset;
import com.png.GridWaveCore.RuleSetNodes.SimpleRuleSetAsset;
import com.png.GridWaveCore.SeedNodes.*;
import com.png.GridWaveCore.TileNodes.*;

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

        AssetRegistry.register(HytaleAssetStore.builder(TileSetAsset.class, new DefaultAssetMap<String, TileSetAsset>())
                .setPath("HytaleGenerator/TileSets")
                .setCodec(TileSetAsset.CODEC)
                .setKeyFunction(TileSetAsset::getId)
                .build()
        );

        //Algo Nodes
        PropDistributionAsset.CODEC.register("PropDistributionAlgo", PropDistributionAlgoAsset.class, PropDistributionAlgoAsset.CODEC);
        PropAsset.CODEC.register("PropAlgo", PropAlgoAsset.class, PropAlgoAsset.CODEC);
        PositionProviderAsset.CODEC.register("GridGen2D", GridGen2DAsset.class, GridGen2DAsset.CODEC);

        //RuleSet Nodes
        RuleSetAsset.CODEC.register("Simple",SimpleRuleSetAsset.class, SimpleRuleSetAsset.CODEC);
        RuleSetAsset.CODEC.register("Advanced", AdvancedRuleSetAsset.class, AdvancedRuleSetAsset.CODEC);

        //TileSet Nodes
        TileSetAsset.CODEC.register("Single", SingleTileSetAsset.class, SingleTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Multi", MultiTileSetAsset.class, MultiTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Auto", AutoTileSetAsset.class, AutoTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Prop", PropTileSetAsset.class, PropTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Imported", ImportedTileSetAsset.class, ImportedTileSetAsset.CODEC);

        //Global Feature Nodes
        FeatureAsset.CODEC.register("Border", BorderAsset.class, BorderAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Debug", DebugAsset.class, DebugAsset.CODEC); //Global
        FeatureAsset.CODEC.register("MultiAttempt", MultiAttemptAsset.class, MultithreadingAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Multithreading", MultiAttemptAsset.class, MultithreadingAsset.CODEC); //Global
        FeatureAsset.CODEC.register("OverlapTile", OverlapTileAsset.class, OverlapTileAsset.CODEC); //Global and Local
        FeatureAsset.CODEC.register("PathKey", PathKeyAsset.class, PathKeyAsset.CODEC); //Global
        FeatureAsset.CODEC.register("PositionRestriction", PositionRestrictionAsset.class, PositionRestrictionAsset.CODEC); //Local


        //Seed Nodes
        SeedAsset.CODEC.register("Constant", ConstantSeedAsset.class, ConstantSeedAsset.CODEC);
        SeedAsset.CODEC.register("Position", PositionSeedAsset.class, PositionSeedAsset.CODEC);
        SeedAsset.CODEC.register("Time", TimeSeedAsset.class, TimeSeedAsset.CODEC);
        SeedAsset.CODEC.register("Imported", ImportedSeedAsset.class, ImportedSeedAsset.CODEC);


        //Currently Unused Nodes
        PropAsset.CODEC.register("StaticMirror", StaticMirrorPropAsset.class, StaticMirrorPropAsset.CODEC);
    }
}
