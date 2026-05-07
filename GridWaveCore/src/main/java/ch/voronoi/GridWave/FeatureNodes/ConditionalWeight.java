package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.ComparisonType;
import ch.voronoi.GridWave.AlgoNodes.Helper.ValueSource;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.TileNodes.TileSet;
import ch.voronoi.GridWave.TileNodes.TileSetAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ConditionalWeight extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<ConditionalWeight> CODEC = BuilderCodec.builder(ConditionalWeight.class, ConditionalWeight::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Value1Source", ValueSource.CODEC), (asset, v) -> asset.value1Source = v, asset -> asset.value1Source)
            .add()
            .append(new KeyedCodec<>("ComparisonType", ComparisonType.CODEC), (asset, v) -> asset.comparisonType = v, asset -> asset.comparisonType)
            .add()
            .append(new KeyedCodec<>("Value2", Codec.FLOAT), (asset, v) -> asset.value2 = v, asset -> asset.value2)
            .add()
            .append(new KeyedCodec<>("WeightIfTrue", Codec.FLOAT), (asset, v) -> asset.weightIfTrue = v, asset -> asset.weightIfTrue)
            .add()
            .build();

    public ValueSource value1Source;
    public ComparisonType comparisonType;
    public float value2;
    public float weightIfTrue;

    @Override
    public void ReplaceWeight(AtomicReference<Double> modifiableWeight, TileSet.TileEntry tileEntry, Map<Vector3i, WaveCell> wave, TileSetAsset.Argument argument) {
        if(skip()) return;
        float value1 = switch (value1Source){
            case CollapsedPercentage -> 100f / wave.size() * wave.values().stream().filter(WaveCell::isCollapsed).count();
        };
        if (comparisonType.satisfies(value1, value2)){
            modifiableWeight.set((double)weightIfTrue);
        }
    }
}
