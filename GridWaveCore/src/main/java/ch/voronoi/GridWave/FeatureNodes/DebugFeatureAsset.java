package ch.voronoi.GridWave.FeatureNodes;

import ch.voronoi.GridWave.AlgoNodes.Helper.AttemptBehavior;
import ch.voronoi.GridWave.AlgoNodes.Helper.GridTileType;
import ch.voronoi.GridWave.AlgoNodes.Helper.WaveCell;
import ch.voronoi.GridWave.AlgoNodes.Helper.TileEntry;
import ch.voronoi.GridWave.TileSetNodes.TileSetAsset;
import ch.voronoi.GridWave.Utils.CuboidWireframe.WireframeCuboidProp;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.ConstantMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.ManualProp;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.UnionProp;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DebugFeatureAsset extends FeatureAsset {
    @Nonnull
    public static final BuilderCodec<DebugFeatureAsset> CODEC = BuilderCodec.builder(
                    DebugFeatureAsset.class, DebugFeatureAsset::new, FeatureAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("WriteToConsole", Codec.BOOLEAN), (asset, v) -> asset.writeToConsole = v, asset -> asset.writeToConsole)
            .add()
            .append(new KeyedCodec<>("ShowNotification", Codec.BOOLEAN), (asset, v) -> asset.showNotification = v, asset -> asset.showNotification)
            .add()
            .append(new KeyedCodec<>("VisualizeGridPositions", Codec.BOOLEAN), (asset, v) -> asset.visualizeGridPositions = v, asset -> asset.visualizeGridPositions)
            .add()
            .append(new KeyedCodec<>("VisualizeSectionBounds", Codec.BOOLEAN), (asset, v) -> asset.visualizeSectionBounds = v, asset -> asset.visualizeSectionBounds)
            .add()
            .append(new KeyedCodec<>("DebugGrid", Codec.BOOLEAN), (asset, v) -> asset.debugGrid = v, asset -> asset.debugGrid)
            .add()
            .append(new KeyedCodec<>("SkipFancyTiles", Codec.BOOLEAN), (asset, v) -> asset.skipFancyTiles = v, asset -> asset.skipFancyTiles)
            .add()
            .append(new KeyedCodec<>("LimitSteps", Codec.BOOLEAN), (asset, v) -> asset.limitSteps = v, asset -> asset.limitSteps)
            .add()
            .append(new KeyedCodec<>("MaxSteps", Codec.INTEGER), (asset, v) -> asset.maxSteps = v, asset -> asset.maxSteps)
            .add()
            .build();

    private boolean writeToConsole = false;
    private boolean showNotification = false;
    private boolean visualizeGridPositions = false;
    private boolean visualizeSectionBounds = false;
    private boolean debugGrid;
    private boolean skipFancyTiles = false;
    private boolean limitSteps;
    private int maxSteps;

    public boolean getWriteToConsole() { return writeToConsole;}
    public boolean getShowNotification() { return showNotification;}
    public boolean getSkipFancyTiles() { return skipFancyTiles; }

    @Override
    public void BeforeWFC(AttemptBehavior attemptBehavior, TileSetAsset.Argument argument) {
        if(limitSteps) attemptBehavior.maxCollapsedCount = maxSteps;
    }

    /** Generates a simplified wave for testing purposes, chronologically collapsing all tiles
     * bottom left to top right and loops through all tile variants (rotations)
     * @return if it had replaced the baseWave
     * */
    @Override
    public boolean WFCReplacer(Map<Vector3ic, WaveCell> baseWave, TileSetAsset.Argument argument) {
        if(skip() || !debugGrid) return false;
        sortByXThenZ(baseWave);
        int counter = 0;
        for(Map.Entry<Vector3ic, WaveCell> entry : baseWave.entrySet()){
            if (entry.getValue().isCollapsed()) continue;
            List<TileEntry> possibles = new ArrayList<>(entry.getValue().possible);
            if(possibles.isEmpty()) continue;
            entry.getValue().setChosen(possibles.get(counter % possibles.size()), GridTileType.BASIC);
            counter++;
        }
        return true;
    }

    private void sortByXThenZ(Map<Vector3ic, WaveCell> baseWave) {
        baseWave.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<Vector3ic, WaveCell> e) -> e.getKey().x())
                        .thenComparingInt(e -> e.getKey().z()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public void AfterPropCreation(AtomicReference<Prop> propReference, TileEntry entry, TileSetAsset.Argument argument) {
        if(skip() || !visualizeGridPositions) return;
        SolidMaterial solid = argument.materialCache.getSolidMaterial("Cloth_Block_Wool_Red");

        Prop manualProp = new ManualProp(new ArrayList<>(List.of(new ManualProp.Block(
            new Material(solid, argument.materialCache.EMPTY_FLUID),new Vector3i(0,propReference.get().getWriteBounds_voxelGrid().max.y() + 1,0).add(entry.getPoiKey()) //Reconstruct this another way!
        ))));
        propReference.set(new UnionProp(new ArrayList<>(List.of(propReference.get(), manualProp))));
    }

    @Override
    public Map<Vector3d, Prop> GetExtraProps(@Nonnull Bounds3i bounds, @Nonnull TileSetAsset.Argument argument) {
        Map<Vector3d, Prop> extraProps = new LinkedHashMap<>();
        if(visualizeSectionBounds){
            SolidMaterial solid = argument.materialCache.getSolidMaterial("Cloth_Block_Wool_Black");
            extraProps.put(Vector3iUtil.toVector3d(bounds.min), new WireframeCuboidProp(bounds.clone().offsetOpposite(bounds.min), new ConstantMaterialProvider<>(new Material(solid, argument.materialCache.EMPTY_FLUID))));
        }
        return extraProps;
    }

}
