package ch.voronoi.GridWave.RuleSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.ElevationRulesAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static ch.voronoi.GridWave.TileSetNodes.TileSetAsset.buildRuleCombo;

public class StringRuleSetAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<StringRuleSetAsset> CODEC = BuilderCodec.builder(StringRuleSetAsset.class, StringRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Str", Codec.STRING, true), (op, val) -> op.str = val, op -> op.str)
            .add()
            .append(new KeyedCodec<>("ElevationRules", ElevationRulesAsset.CODEC, false), (op, val) -> op.elevationRulesAsset = val, op -> op.elevationRulesAsset)
            .add()
            .build();


    private String str;
    private ElevationRulesAsset elevationRulesAsset = null;

    @Override
    public List<RuleCombo> build() {
        var elevationRules = elevationRulesAsset == null ? null : elevationRulesAsset.elevationRules;
        return Arrays.stream(buildRuleCombo(str, elevationRules)).toList();
    }
}
