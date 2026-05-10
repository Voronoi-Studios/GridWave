package ch.voronoi.GridWave.TileSetNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

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
    public List<TileSet> build(@Nonnull TileSetAsset.Argument argument) {
        List<AutoTileSetAsset> autoTileSetAssets = new ArrayList<>();
        Map<Path, Path> map = TileSetAsset.getPackToFullPathsMap(folderPath, false);

        for (Map.Entry<Path, Path> e : map.entrySet()) {
            Path packPath = e.getKey(); Path fullFolderPath = e.getValue();
            Path sub = fullFolderPath.subpath(packPath.getNameCount(), fullFolderPath.getNameCount());
            autoTileSetAssets.add(new AutoTileSetAsset(sub.toString(), super.tileFeatureAssets));
        }

        List<TileSet> tileSets = new ArrayList<>();
        for(TileSetAsset tileSetAsset : autoTileSetAssets){
            tileSets.addAll(tileSetAsset.build(argument));
        }
        return tileSets;
    }
}
