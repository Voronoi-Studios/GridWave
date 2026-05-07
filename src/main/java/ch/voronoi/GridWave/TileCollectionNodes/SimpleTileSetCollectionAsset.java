package ch.voronoi.GridWave.TileCollectionNodes;

import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SimpleTileSetCollectionAsset extends TileSetCollectionAsset {
    @Nonnull
    public static final BuilderCodec<SimpleTileSetCollectionAsset> CODEC = BuilderCodec.builder(SimpleTileSetCollectionAsset.class, SimpleTileSetCollectionAsset::new, TileSetCollectionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("TileSets", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, value) -> asset.tileSetAssets = value, asset -> asset.tileSetAssets)
            .add()
            .build();

    private TileSetAsset[] tileSetAssets = new TileSetAsset[0];

    @Override
    public List<TileSet.TileEntry> build(@Nonnull TileSetAsset.Argument argument) {
        List<TileSet.TileEntry> tileEntries = new ArrayList<>();
        for(TileSetAsset tileSetAsset : tileSetAssets){
            TileSet result = tileSetAsset.build(argument);
            tileEntries.addAll(result.getAllTileEntries());
        }
        return tileEntries;
    }

    @Override
    public TileSetAsset[] getTileSetAssets() { return tileSetAssets; }
}
