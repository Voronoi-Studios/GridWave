package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;

import javax.annotation.Nonnull;
import java.util.List;

public class RestrainerAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<RestrainerAsset> CODEC = BuilderCodec.builder(
                    RestrainerAsset.class, RestrainerAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Pos", Vector3i.CODEC, true), (asset, value) -> asset.pos = value, asset -> asset.pos)
            .add()
            .append(new KeyedCodec<>("Rot", Rotation.CODEC, true), (asset, value) -> asset.rot = value, asset -> asset.rot)
            .add()
            .build();

    public Vector3i pos = Vector3i.ZERO.clone();
    public Rotation rot = Rotation.None;

    @Override
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries, TileSetAsset.Argument argument) {
        if(skip()) return;
        int rot = this.rot.getDegrees() / 90;
        TileSet.TileEntry tileEntry = tileEntries.get(rot%tileEntries.size());
        tileEntry = TileSet.offsetTileEntry(tileEntry, pos);
        tileEntries.clear();
        tileEntries.add(tileEntry);
    }
}
