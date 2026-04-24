package com.png.GridWaveCore.FeatureNodes;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.png.GridWaveCore.AlgoNodes.Helper.AttemptBehavior;
import com.png.GridWaveCore.AlgoNodes.Helper.WaveCell;
import com.png.GridWaveCore.AlgoNodes.IAlgoAsset;
import com.png.GridWaveCore.TileNodes.TileSet;
import com.png.GridWaveCore.TileNodes.TileSetAsset;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public abstract class FeatureAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, FeatureAsset>> {
    @Nonnull
    public static final AssetCodecMapCodec<String, FeatureAsset> CODEC = new AssetCodecMapCodec<>(
            Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );
    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(FeatureAsset.class, CODEC);
    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
    @Nonnull
    public static final BuilderCodec<FeatureAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(FeatureAsset.class)
            .append(new KeyedCodec<>("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, t -> t.skip)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private boolean skip = false;


    public String getId() {
        return this.id;
    }

    public boolean skip() {
        return this.skip;
    }

    @Override
    public void cleanUp() {
    }

    //Implement different methods
    public void AfterTileSetCreation(List<TileSet.TileEntry> tileEntries, TileSetAsset.Argument argument) { }

    public void BaseWaveProcessor(@NonNull List<Vector3d> gridPositions, int grid, Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) { }

    public void AfterNeighbourPropagation(WaveCell source, int rot, WaveCell neighbor, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) { }

    public boolean WFCReplacer(Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) { return false; }

    public boolean FinalCheck(Map<Vector3i, WaveCell> baseWave, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) { return true; }

    public void BeforeWFC(AttemptBehavior attemptBehavior, List<FeatureAsset> featureAssets, IAlgoAsset algoAsset) { }
}
