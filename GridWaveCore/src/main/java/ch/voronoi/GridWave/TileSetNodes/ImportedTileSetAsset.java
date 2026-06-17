package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.FeatureNodes.FeatureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.EmptyProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ImportedTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<ImportedTileSetAsset> CODEC = BuilderCodec.builder(
                    ImportedTileSetAsset.class, ImportedTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importName = k, k -> k.importName)
            .add()
            .build();
    private String importName = "";

    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument, FeatureAsset... addFeatures) {
        if (this.importName != null && !this.importName.isEmpty()) {
            TileSetAsset exported = getExportedAsset(this.importName);
            if(exported != null) {
                return exported.build(argument,Stream.concat(Arrays.stream(this.tileFeatureAssets), Arrays.stream(addFeatures)).toArray(FeatureAsset[]::new));
            }
            else {
                HytaleLogger.getLogger().atWarning().log("An exported TileSet with this name does not exist: " + this.importName);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
