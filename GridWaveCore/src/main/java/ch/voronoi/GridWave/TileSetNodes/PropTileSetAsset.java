package ch.voronoi.GridWave.TileSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.EmptyPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import ch.voronoi.GridWave.RuleSetNodes.RuleSetAsset;
import ch.voronoi.GridWave.RuleSetNodes.SimpleRuleSetAsset;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropTileSetAsset extends TileSetAsset {
    @Nonnull
    public static final BuilderCodec<PropTileSetAsset> CODEC = BuilderCodec.builder(PropTileSetAsset.class, PropTileSetAsset::new, TileSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSets", new ArrayCodec<>(RuleSetAsset.CODEC, RuleSetAsset[]::new), true), (asset, value) -> asset.ruleSetAssets = value, asset -> asset.ruleSetAssets)
            .add()
            .append(new KeyedCodec<>("Prop", PropAsset.CODEC, true),
                    (asset, v) -> asset.propAsset = v,
                    asset -> asset.propAsset
            ).add()
            .append(new KeyedCodec<>("SizeZ", Codec.INTEGER, true), (asset, value) -> asset.zSize = value, asset -> asset.zSize)
            .add()
            .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
            .build();
    private RuleSetAsset[] ruleSetAssets = new SimpleRuleSetAsset[0];
    private PropAsset propAsset = new EmptyPropAsset();
    private int zSize;
    private double weight = 1;

    @Nonnull
    @Override
    public List<TileSet> build(@Nonnull Argument argument) {
        Map<Vector3i, RuleCombo> ruleSets = new HashMap<>();
        Vector3i offset = Vector3i.ZERO.clone();
        for(RuleSetAsset ruleSetAsset : ruleSetAssets){
            ruleSets.put(offset.clone().scale(argument.algoAsset.getGrid()), ruleSetAsset.build());
            offset.z++;
            if(offset.z >= zSize) {
                offset.z = 0;
                offset.x--;
            }
        }

        return new ArrayList<>(List.of(new PropTileSet(propAsset, ruleSets, weight, argument, super.getTileFeatureAssets())));
    }
}
