package ch.voronoi.GridWave;

import ch.voronoi.GridWave.AlgoNodes.PropAlgoAsset;
import ch.voronoi.GridWave.AlgoNodes.PropDistributionAlgoAsset;
import ch.voronoi.GridWave.FeatureNodes.*;
import ch.voronoi.GridWave.RuleSetNodes.AdvancedRuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.StringRuleSetAsset;
import ch.voronoi.GridWave.SeedNodes.ConstantSeedAsset;
import ch.voronoi.GridWave.SeedNodes.ImportedSeedAsset;
import ch.voronoi.GridWave.SeedNodes.SeedAsset;
import ch.voronoi.GridWave.SeedNodes.TimeSeedAsset;
import ch.voronoi.GridWave.TileSetNodes.*;
import ch.voronoi.GridWave.Utils.CuboidWireframe.WireframeCuboidPropAsset;
import ch.voronoi.GridWave.Utils.GridGen.*;
import ch.voronoi.GridWave.Utils.MirrorNode.StaticMirrorPropAsset;
import ch.voronoi.GridWave._Commands.GridWaveCoreCommand;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propdistribution.PropDistributionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CorePlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CorePlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        //Test stuff
        this.getCommandRegistry().registerCommand(new GridWaveCoreCommand(this.getName(), this.getManifest().getVersion().toString(), this.getFile(), getPatchTarget()));

        AssetRegistry.register(HytaleAssetStore.builder(TileSetAsset.class, new DefaultAssetMap<String, TileSetAsset>())
                .setPath("HytaleGenerator/TileSets")
                .setCodec(TileSetAsset.CODEC)
                .setKeyFunction(TileSetAsset::getId)
                .build()
        );

        AssetRegistry.register(HytaleAssetStore.builder(FeatureAsset.class, new DefaultAssetMap<String, FeatureAsset>())
                .setPath("HytaleGenerator/Features")
                .setCodec(FeatureAsset.CODEC)
                .setKeyFunction(FeatureAsset::getId)
                .build()
        );


        //Algo Nodes
        PropDistributionAsset.CODEC.register("PropDistributionAlgo", PropDistributionAlgoAsset.class, PropDistributionAlgoAsset.CODEC);
        PropAsset.CODEC.register("PropAlgo", PropAlgoAsset.class, PropAlgoAsset.CODEC);

        //RuleSet Nodes
        RuleSetAsset.CODEC.register("Simple", SimpleRuleSetAsset.class, SimpleRuleSetAsset.CODEC);
        RuleSetAsset.CODEC.register("Advanced", AdvancedRuleSetAsset.class, AdvancedRuleSetAsset.CODEC);
        RuleSetAsset.CODEC.register("String", StringRuleSetAsset.class, StringRuleSetAsset.CODEC);
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
        FeatureAsset.CODEC.register("SectionStorage", SectionStorageAsset.class, SectionStorageAsset.CODEC); //Global

        //Seed Nodes
        SeedAsset.CODEC.register("Constant", ConstantSeedAsset.class, ConstantSeedAsset.CODEC);
        SeedAsset.CODEC.register("Time", TimeSeedAsset.class, TimeSeedAsset.CODEC);
        SeedAsset.CODEC.register("Imported", ImportedSeedAsset.class, ImportedSeedAsset.CODEC);

        //Util Nodes
        PropAsset.CODEC.register("WireframeCuboid", WireframeCuboidPropAsset.class, WireframeCuboidPropAsset.CODEC);
        PropAsset.CODEC.register("StaticMirror", StaticMirrorPropAsset.class, StaticMirrorPropAsset.CODEC);
        PositionProviderAsset.CODEC.register("GridGen", GridGenAsset.class, GridGenAsset.CODEC);
        CustomBoundsAsset.CODEC.register("Grid", GridBoundsAsset.class, GridBoundsAsset.CODEC);
        CustomBoundsAsset.CODEC.register("Converter", BoundsConverterAsset.class, BoundsConverterAsset.CODEC);
    }

    private static @Nullable Path getPatchTarget() {
        Path subPath = Paths.get("install/release/package/game/latest/Client/NodeEditor/Workspaces/HytaleGenerator Java");

        List<Path> candidates = List.of(
                // Windows
                Paths.get(System.getenv().getOrDefault("APPDATA", ""), "Hytale"),
                // Linux
                PathUtil.getUserHome().resolve(".config").resolve("Hytale"),
                PathUtil.getUserHome().resolve(".local/share/Hytale"),
                PathUtil.getUserHome().resolve(".var/app/com.hypixel.HytaleLauncher/data/Hytale"),
                // Mac
                PathUtil.getUserHome().resolve("Library/Application Support/Hytale")
        );

        for (Path base : candidates) {
            Path target = base.resolve(subPath);
            if (Files.isDirectory(target)) return target;
        }
        return null;
    }
}
