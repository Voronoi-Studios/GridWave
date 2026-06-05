package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileSetGroupAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<TileSetGroupAsset> CODEC = BuilderCodec.builder(TileSetGroupAsset.class, TileSetGroupAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("TileSets", new ArrayCodec<>(TileSetAsset.CODEC, TileSetAsset[]::new), true), (asset, value) -> asset.tileSetAssets = value, asset -> asset.tileSetAssets)
            .add()
            .build();

    private TileSetAsset[] tileSetAssets = new TileSetAsset[0];

    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument, FeatureAsset... addFeatures) {
        List<TileSet> tileSets = new ArrayList<>();
        for(TileSetAsset tileSetAsset : tileSetAssets){
            tileSets.addAll(tileSetAsset.build(argument));
        }
        return tileSets;
    }
}
