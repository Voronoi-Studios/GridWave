package ch.voronoi.GridWave.TileCollectionNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import ch.voronoi.GridWave.RuleSetNodes.RuleSet;
import ch.voronoi.GridWave.TileNodes.AutoTileSetAsset;
import ch.voronoi.GridWave.TileNodes.MultiTileSet;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class AutoTileSetCollectionAsset extends TileSetCollectionAsset {
    @Nonnull
    public static final BuilderCodec<AutoTileSetCollectionAsset> CODEC = BuilderCodec.builder(AutoTileSetCollectionAsset.class, AutoTileSetCollectionAsset::new, TileSetCollectionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Features", new ArrayCodec<>(FeatureAsset.CODEC, FeatureAsset[]::new), false),
                    (asset, v) -> asset.tileFeatureAssets = v,
                    asset -> asset.tileFeatureAssets)
            .add()
            .append(new KeyedCodec<>("FolderPath", Codec.STRING, true), (t, k) -> t.folderPath = k, k -> k.folderPath)
            .documentation("Uses the immediate child folders naming to create the rulesets for the MultiTiles\nExample: `Maze/FancyTiles` it will then query any subfolders like for example `Maze/FancyTiles/1x2/10X0-X010`")
            .add()
            .build();

    private @Nonnull FeatureAsset[] tileFeatureAssets = new FeatureAsset[0];

    private String folderPath;


    @Nonnull
    @Override
    public List<TileSet.TileEntry> build(@Nonnull TileSetAsset.Argument argument) {
        List<AutoTileSetAsset> autoTileSetAssets = new ArrayList<>();
        Map<Path, Path> map = TileSetAsset.getPackToFullPathsMap(folderPath, false);

        for (Map.Entry<Path, Path> e : map.entrySet()) {
            Path packPath = e.getKey(); Path fullFolderPath = e.getValue();
            Path sub = fullFolderPath.subpath(packPath.getNameCount(), fullFolderPath.getNameCount());
            autoTileSetAssets.add(new AutoTileSetAsset(sub.toString(), tileFeatureAssets));
        }

        List<TileSet.TileEntry> tileEntries = new ArrayList<>();
        for(TileSetAsset tileSetAsset : autoTileSetAssets){
            TileSet result = tileSetAsset.build(argument);
            tileEntries.addAll(result.getAllTileEntries());
        }
        return tileEntries;
    }

    @Override
    public TileSetAsset[] getTileSetAssets() {
        List<AutoTileSetAsset> autoTileSetAssets = new ArrayList<>();
        Map<Path, Path> map = TileSetAsset.getPackToFullPathsMap(folderPath, false);

        for (Map.Entry<Path, Path> e : map.entrySet()) {
            Path packPath = e.getKey(); Path fullFolderPath = e.getValue();
            Path sub = fullFolderPath.subpath(packPath.getNameCount(), fullFolderPath.getNameCount());
            autoTileSetAssets.add(new AutoTileSetAsset(sub.toString(), tileFeatureAssets));
        }
        return autoTileSetAssets.toArray(new TileSetAsset[0]);
    }
}
