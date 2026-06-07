package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class RestrainerFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<RestrainerFeatureAsset> CODEC = BuilderCodec.builder(
                    RestrainerFeatureAsset.class, RestrainerFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Pos", Vector3iUtil.CODEC, true), (asset, value) -> asset.pos = value, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public Vector3i pos = new Vector3i(Vector3iUtil.ZERO);
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileEntry> tileEntries, TileSetAsset.Argument argument) {
        if(skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        tileEntry = tileEntry.restrain(pos);

        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
