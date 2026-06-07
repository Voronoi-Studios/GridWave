package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AutoTileSetGroupAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<AutoTileSetGroupAsset> CODEC = BuilderCodec.builder(AutoTileSetGroupAsset.class, AutoTileSetGroupAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("FolderPath", Codec.STRING, true), (t, k) -> t.folderPath = k, k -> k.folderPath)
            .documentation("Uses the immediate child folders naming to create the rulesets for the MultiTiles\nExample: `Maze/FancyTiles` it will then query any subfolders like for example `Maze/FancyTiles/1x2/10X0-X010`")
            .add()
            .build();

    private String folderPath;


    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument, FeatureAsset... addFeatures) {
        Map<Path, Path> map = TileSetAsset.getPackToFullPathsMap(folderPath, false);
        List<AutoTileSetAsset> autoTileSetAssets = new ArrayList<>();

        for (Map.Entry<Path, Path> e : map.entrySet()) {
            if (!Files.isDirectory(e.getValue())) continue;
            collectLeafDirs(e.getValue(), e.getKey(), autoTileSetAssets);
        }

        List<TileSet> tileSets = new ArrayList<>();
        for(AutoTileSetAsset asset : autoTileSetAssets){
            tileSets.addAll(asset.build(argument,addFeatures));
        }
        return tileSets;
    }

    private void collectLeafDirs(Path current, Path packPath, List<AutoTileSetAsset> autoTileSetAssets) {
        File[] subDirs = current.toFile().listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length == 0) {
            String relativePath = current.subpath(packPath.getNameCount()+2, current.getNameCount()).toString();
            autoTileSetAssets.add(new AutoTileSetAsset(relativePath, super.tileFeatureAssets));
            return;
        }
        for (File sub : subDirs) collectLeafDirs(sub.toPath(), packPath, autoTileSetAssets);
    }
}
