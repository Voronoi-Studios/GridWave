package ch.voronoi.GridWave.TileCollectionNodes;

import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnionTileSetCollectionAsset extends TileSetCollectionAsset {
    @Nonnull
    public static final BuilderCodec<UnionTileSetCollectionAsset> CODEC = BuilderCodec.builder(UnionTileSetCollectionAsset.class, UnionTileSetCollectionAsset::new, TileSetCollectionAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("TileSetCollections", new ArrayCodec<>(TileSetCollectionAsset.CODEC, TileSetCollectionAsset[]::new), true), (asset, value) -> asset.tileSetCollectionAssets = value, asset -> asset.tileSetCollectionAssets)
            .add()
            .build();

    private TileSetCollectionAsset[] tileSetCollectionAssets = new TileSetCollectionAsset[0];

    @Override
    public List<TileSet.TileEntry> build(@Nonnull TileSetAsset.Argument argument) {
        List<TileSet.TileEntry> tileEntries = new ArrayList<>();
        for(var tileCollectionAsset : tileSetCollectionAssets){
            tileEntries.addAll(tileCollectionAsset.build(argument));
        }
        return tileEntries;
    }

    public TileSetAsset[] getTileSetAssets() {
        return Arrays.stream(tileSetCollectionAssets)
                .flatMap(c -> Arrays.stream(c.getTileSetAssets()))
                .toArray(TileSetAsset[]::new);    }
}
