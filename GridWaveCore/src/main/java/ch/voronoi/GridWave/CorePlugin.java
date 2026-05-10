package ch.voronoi.GridWave;

import ch.voronoi.GridWave.Utils.GridGen.GridGen2DAsset;
import ch.voronoi.GridWave.AlgoNodes.PropAlgoAsset;
import ch.voronoi.GridWave.AlgoNodes.PropDistributionAlgoAsset;
import ch.voronoi.GridWave.FeatureNodes.*;
import ch.voronoi.GridWave.RuleSetNodes.*;
import ch.voronoi.GridWave.SeedNodes.*;
import ch.voronoi.GridWave.TileSetNodes.*;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import ch.voronoi.GridWave.Utils.MirrorNode.StaticMirrorPropAsset;

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

        //RuleSet Nodes
        RuleSetAsset.CODEC.register("Simple", SimpleRuleSetAsset.class, SimpleRuleSetAsset.CODEC);
        RuleSetAsset.CODEC.register("Advanced", AdvancedRuleSetAsset.class, AdvancedRuleSetAsset.CODEC);
        //SimpleVerticalRules
        //AdvancedVerticalRules
        //ElevationRules

        //TileSet Nodes
        TileSetAsset.CODEC.register("Imported", ImportedTileSetAsset.class, ImportedTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Group", TileSetGroupAsset.class, TileSetGroupAsset.CODEC);
        TileSetAsset.CODEC.register("AutoGroup", AutoTileSetGroupAsset.class, AutoTileSetGroupAsset.CODEC);

        TileSetAsset.CODEC.register("Single", SingleTileSetAsset.class, SingleTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Multi", MultiTileSetAsset.class, MultiTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Auto", AutoTileSetAsset.class, AutoTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Prop", PropTileSetAsset.class, PropTileSetAsset.CODEC);

        //Feature Nodes
        FeatureAsset.CODEC.register("Imported", ImportedFeatureAsset.class, ImportedFeatureAsset.CODEC);
        FeatureAsset.CODEC.register("Group", FeatureGroupAsset.class, FeatureGroupAsset.CODEC);

        FeatureAsset.CODEC.register("Border", BorderFeatureAsset.class, BorderFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Debug", DebugFeatureAsset.class, DebugFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("MultiAttempt", MultiAttemptFeatureAsset.class, MultiAttemptFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Multithreading", MultithreadingFeatureAsset.class, MultithreadingFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("OverlapTile", OverlapTileFeatureAsset.class, OverlapTileFeatureAsset.CODEC); //Global and Local
        FeatureAsset.CODEC.register("PathKey", PathKeyFeatureAsset.class, PathKeyFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Restrainer", RestrainerFeatureAsset.class, RestrainerFeatureAsset.CODEC); //Local
        FeatureAsset.CODEC.register("RandomRestrainer", RandomRestrainerFeatureAsset.class, RandomRestrainerFeatureAsset.CODEC); //Local
        FeatureAsset.CODEC.register("PathCellSelector", PathCellSelectorFeatureAsset.class, PathCellSelectorFeatureAsset.CODEC); //Global
        FeatureAsset.CODEC.register("ConditionalWeight", ConditionalWeightFeatureAsset.class, ConditionalWeightFeatureAsset.CODEC); //Local

        //Seed Nodes
        SeedAsset.CODEC.register("Constant", ConstantSeedAsset.class, ConstantSeedAsset.CODEC);
        SeedAsset.CODEC.register("Time", TimeSeedAsset.class, TimeSeedAsset.CODEC);
        SeedAsset.CODEC.register("Imported", ImportedSeedAsset.class, ImportedSeedAsset.CODEC);

        //Currently Util Nodes
        PropAsset.CODEC.register("StaticMirror", StaticMirrorPropAsset.class, StaticMirrorPropAsset.CODEC);
        PositionProviderAsset.CODEC.register("GridGen2D", GridGen2DAsset.class, GridGen2DAsset.CODEC);
    }
}
