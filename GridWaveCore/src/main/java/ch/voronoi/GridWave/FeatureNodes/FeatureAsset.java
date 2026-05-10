package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.CellSelector;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.AlgoNodes.Helper.AttemptBehavior;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.TileSetNodes.TileSet;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FeatureAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, FeatureAsset>> {
    @Nonnull
    public static final AssetCodecMapCodec<String, FeatureAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );
    private static final Map<String, FeatureAsset.Exported> exportedNodes = new ConcurrentHashMap<>();
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(FeatureAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<FeatureAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(FeatureAsset.class)
            .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
            .add()
            .afterDecode(asset -> {
                if (asset.exportName != null && !asset.exportName.isEmpty()) {
                    if (exportedNodes.containsKey(asset.exportName)) {
                        LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
                    }

                    FeatureAsset.Exported exported = new FeatureAsset.Exported();
                    exported.asset = asset;
                    exportedNodes.put(asset.exportName, exported);
                    LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
                }
            })
            .append(new KeyedCodec<>("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, t -> t.skip)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private String exportName = "";
    private boolean skip = false;

    public List<FeatureAsset> build() { return new ArrayList<>(List.of(this)); }

    public static FeatureAsset.Exported getExportedAsset(@Nonnull String name) {
        return exportedNodes.get(name);
    }

    public String getId() {
        return this.id;
    }

    public static class Exported {
        public FeatureAsset asset;
    }

    public boolean skip() {
        return this.skip;
    }

    @Override
    public void cleanUp() {
    }

    //Implement different methods
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries, TileSetAsset.Argument argument) { }

    public void BaseWaveProcessor(@NonNull List<Vector3d> gridPositions, Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) { }

    public boolean WFCReplacer(Map<Vector3i, WaveCell> baseWave, TileSetAsset.Argument argument) { return false; }

    public boolean FinalCheck(Map<Vector3i, WaveCell> baseWave, int participantNumber, TileSetAsset.Argument argument) { return true; }

    public void BeforeWFC(AttemptBehavior attemptBehavior, TileSetAsset.Argument argument) { }

    public void ReplaceCellSelector(AtomicReference<CellSelector> cellSelector, TileSetAsset.Argument argument) { }

    public void ReplaceWeight(AtomicReference<Double> modifiableWeight, TileSet.TileEntry tileEntry, Map<Vector3i, WaveCell> wave, TileSetAsset.Argument argument) {}
}
