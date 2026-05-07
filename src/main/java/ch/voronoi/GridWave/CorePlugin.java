package ch.voronoi.GridWave;

import ch.voronoi.GridWave.AlgoNodes.GridGen2DAsset;
import ch.voronoi.GridWave.AlgoNodes.PropAlgoAsset;
import ch.voronoi.GridWave.AlgoNodes.PropDistributionAlgoAsset;
import ch.voronoi.GridWave.FeatureNodes.*;
import ch.voronoi.GridWave.RuleSetNodes.*;
import ch.voronoi.GridWave.SeedNodes.*;
import ch.voronoi.GridWave.TileCollectionNodes.*;
import ch.voronoi.GridWave.TileNodes.*;
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

        AssetRegistry.register(HytaleAssetStore.builder(TileSetCollectionAsset.class, new DefaultAssetMap<String, TileSetCollectionAsset>())
                .setPath("HytaleGenerator/TileSetCollections")
                .setCodec(TileSetCollectionAsset.CODEC)
                .setKeyFunction(TileSetCollectionAsset::getId)
                .build()
        );

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
        RuleSetAsset.CODEC.register("Simple2D", SimpleRuleSet2DAsset.class, SimpleRuleSet2DAsset.CODEC);
        RuleSetAsset.CODEC.register("Simple3D", SimpleRuleSet3DAsset.class, SimpleRuleSet3DAsset.CODEC);
        RuleSetAsset.CODEC.register("Advanced2D", AdvancedRuleSet2DAsset.class, AdvancedRuleSet2DAsset.CODEC);
        RuleSetAsset.CODEC.register("Advanced3D", AdvancedRuleSet3DAsset.class, AdvancedRuleSet3DAsset.CODEC);

        //TileSet Collection Nodes
        TileSetCollectionAsset.CODEC.register("Simple", SimpleTileSetCollectionAsset.class, SimpleTileSetCollectionAsset.CODEC);
        TileSetCollectionAsset.CODEC.register("Auto", AutoTileSetCollectionAsset.class, AutoTileSetCollectionAsset.CODEC);
        TileSetCollectionAsset.CODEC.register("Union", UnionTileSetCollectionAsset.class, UnionTileSetCollectionAsset.CODEC);
        TileSetCollectionAsset.CODEC.register("Imported", ImportedTileSetCollectionAsset.class, ImportedTileSetCollectionAsset.CODEC);

        //TileSet Nodes
        TileSetAsset.CODEC.register("Single", SingleTileSetAsset.class, SingleTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Multi", MultiTileSetAsset.class, MultiTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Auto", AutoTileSetAsset.class, AutoTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Prop", PropTileSetAsset.class, PropTileSetAsset.CODEC);
        TileSetAsset.CODEC.register("Imported", ImportedTileSetAsset.class, ImportedTileSetAsset.CODEC);

        //Global Feature Nodes
        FeatureAsset.CODEC.register("Border", BorderAsset.class, BorderAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Debug", DebugAsset.class, DebugAsset.CODEC); //Global
        FeatureAsset.CODEC.register("MultiAttempt", MultiAttemptAsset.class, MultiAttemptAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Multithreading", MultithreadingAsset.class, MultithreadingAsset.CODEC); //Global
        FeatureAsset.CODEC.register("OverlapTile", OverlapTileAsset.class, OverlapTileAsset.CODEC); //Global and Local
        FeatureAsset.CODEC.register("PathKey", PathKeyAsset.class, PathKeyAsset.CODEC); //Global
        FeatureAsset.CODEC.register("Restrainer", RestrainerAsset.class, RestrainerAsset.CODEC); //Local
        FeatureAsset.CODEC.register("RandomRestrainer", RandomRestrainerAsset.class, RandomRestrainerAsset.CODEC); //Local
        FeatureAsset.CODEC.register("PathCellSelector", PathCellSelectorAsset.class, PathCellSelectorAsset.CODEC); //Global
        FeatureAsset.CODEC.register("ConditionalWeight", ConditionalWeight.class, ConditionalWeight.CODEC); //Local

        //Seed Nodes
        SeedAsset.CODEC.register("Constant", ConstantSeedAsset.class, ConstantSeedAsset.CODEC);
        SeedAsset.CODEC.register("Time", TimeSeedAsset.class, TimeSeedAsset.CODEC);
        SeedAsset.CODEC.register("Imported", ImportedSeedAsset.class, ImportedSeedAsset.CODEC);


        //Currently Unused Nodes
        PropAsset.CODEC.register("StaticMirror", StaticMirrorPropAsset.class, StaticMirrorPropAsset.CODEC);
    }
}
