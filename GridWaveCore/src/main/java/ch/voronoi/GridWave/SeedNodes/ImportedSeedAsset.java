package ch.voronoi.GridWave.SeedNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import ch.voronoi.GridWave.AlgoNodes.Helper.IAlgoAsset;

import javax.annotation.Nonnull;

public class ImportedSeedAsset extends SeedAsset {
    @Nonnull
    public static final BuilderCodec<ImportedSeedAsset> CODEC = BuilderCodec.builder(
                    ImportedSeedAsset.class, ImportedSeedAsset::new, SeedAsset.ABSTRACT_CODEC
            )
            .append(new KeyedCodec<>("Name", Codec.STRING, true), (t, k) -> t.name = k, k -> k.name)
            .add()
            .build();
    private String name = "";

    @Override
    public String build(IAlgoAsset algoAsset) {
        if (this.name != null && !this.name.isEmpty()) {
            SeedAsset exportedAsset = SeedAsset.getExportedAsset(this.name);
            return exportedAsset == null ? null : exportedAsset.build(algoAsset);
        } else {
            HytaleLogger.getLogger().atWarning().log("An exported Seed with the name does not exist: " + this.name);
            return null;
        }
    }
}
