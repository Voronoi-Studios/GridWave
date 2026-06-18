package ch.voronoi.GridWave.Utils.GridGen;

import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

import static ch.voronoi.GridWave.Utils.GridGen.GridGen.createBounds;

public class GridGenAsset extends PositionProviderAsset {
    @Nonnull
    public static final BuilderCodec<GridGenAsset> CODEC = BuilderCodec.builder(GridGenAsset.class, GridGenAsset::new, PositionProviderAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Position", Vector3iUtil.CODEC, false), (asset, v) -> asset.pos = v, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Offset", Vector3iUtil.CODEC, true), (asset, v) -> asset.offset = v, asset -> asset.offset)
            .add()
            .append(new KeyedCodec<>("Repeat", Vector3iUtil.CODEC, true), (asset, v) -> asset.repeat = v, asset -> asset.repeat)
            .add()
            .append(new KeyedCodec<>("CenterHorizontally", Codec.BOOLEAN, true), (asset, v) -> asset.centerHorizontally = v, asset -> asset.centerHorizontally)
            .add()
            .build();

    private Vector3i pos = new Vector3i(Vector3iUtil.ZERO);
    private Vector3i offset = new Vector3i();
    private Vector3i repeat = new Vector3i();
    private boolean centerHorizontally;

    @Override
    public PositionProvider build(@Nonnull Argument argument) {
        return new GridGen(pos, Vector3iUtil.max(offset, Vector3iUtil.ALL_ONES), Vector3iUtil.max(repeat, Vector3iUtil.ALL_ONES), centerHorizontally);
    }

    public Vector3i getGrid() {
        return Vector3iUtil.max(offset, Vector3iUtil.ALL_ONES);
    }

    public Bounds3i getBounds() {
        return createBounds(pos, Vector3iUtil.max(offset, Vector3iUtil.ALL_ONES), Vector3iUtil.max(repeat, Vector3iUtil.ALL_ONES), centerHorizontally);
    }
}
