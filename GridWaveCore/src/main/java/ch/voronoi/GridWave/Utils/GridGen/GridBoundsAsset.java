package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class GridBoundsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, GridBoundsAsset>> {
    @Nonnull
    public static final AssetBuilderCodec<String, GridBoundsAsset> CODEC = AssetBuilderCodec.builder(
                    GridBoundsAsset.class,
                    GridBoundsAsset::new,
                    Codec.STRING,
                    (asset, id) -> asset.id = id,
                    config -> config.id,
                    (config, data) -> config.data = data,
                    config -> config.data
            )
            .append(new KeyedCodec<>("Position", Vector3i.CODEC, false), (asset, v) -> asset.pos = v, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Offset", Vector3i.CODEC, true), (asset, v) -> asset.offset = v, asset -> asset.offset)
            .add()
            .append(new KeyedCodec<>("Repeat", Vector3i.CODEC, true), (asset, v) -> asset.repeat = v, asset -> asset.repeat)
            .add()
            .append(new KeyedCodec<>("Centered", Codec.BOOLEAN, true), (asset, v) -> asset.centeredOnPosition = v, asset -> asset.centeredOnPosition)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;

    private Vector3i pos = new Vector3i();
    private Vector3i offset = new Vector3i();
    private Vector3i repeat = new Vector3i();
    private boolean centeredOnPosition;

    @Nonnull
    public Bounds3i build() {
        return GridGen.createBounds(pos, offset, repeat, centeredOnPosition);
    }

    public String getId() {
        return this.id;
    }
}
