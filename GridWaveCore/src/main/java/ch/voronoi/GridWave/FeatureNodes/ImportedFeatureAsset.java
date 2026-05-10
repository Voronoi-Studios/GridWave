package ch.voronoi.GridWave.FeatureNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ImportedFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<ImportedFeatureAsset> CODEC = BuilderCodec.builder(
                    ImportedFeatureAsset.class, ImportedFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.importName = k, k -> k.importName)
            .add()
            .build();
    private String importName = "";

    @Override
    public List<FeatureAsset> build() {
        if (this.importName != null && !this.importName.isEmpty()) {
            FeatureAsset.Exported exported = getExportedAsset(this.importName);
            if(exported != null && exported.asset != null){ return exported.asset.build(); }
            else {
                HytaleLogger.getLogger().atWarning().log("An exported Feature with this name does not exist: " + this.importName);
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
