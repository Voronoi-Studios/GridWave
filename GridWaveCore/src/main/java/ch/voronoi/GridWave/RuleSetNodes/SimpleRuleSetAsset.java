package ch.voronoi.GridWave.RuleSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.HorizontalRules;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleSet;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.ElevationRulesAsset;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.SimpleVerticalRulesAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SimpleRuleSetAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<SimpleRuleSetAsset> CODEC = BuilderCodec.builder(SimpleRuleSetAsset.class, SimpleRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("HorizontalRules", HorizontalRules.CODEC, true), (op, val) -> op.horizontalRules = val, op -> op.horizontalRules)
            .add()
            .append(new KeyedCodec<>("VerticalRules", SimpleVerticalRulesAsset.CODEC, false), (op, val) -> op.simpleVerticalRulesAsset = val, op -> op.simpleVerticalRulesAsset)
            .add()
            .append(new KeyedCodec<>("ElevationRules", ElevationRulesAsset.CODEC, false), (op, val) -> op.elevationRulesAsset = val, op -> op.elevationRulesAsset)
            .add()
            .build();


    private HorizontalRules horizontalRules = new HorizontalRules();
    private SimpleVerticalRulesAsset simpleVerticalRulesAsset = null;
    private ElevationRulesAsset elevationRulesAsset = null;

    @Override
    public List<RuleCombo> build() {
        var verticalRules = simpleVerticalRulesAsset == null ? null : simpleVerticalRulesAsset.verticalRules;
        var elevationRules = elevationRulesAsset == null ? null : elevationRulesAsset.elevationRules;
        return new ArrayList<>(List.of(new RuleCombo(new RuleSet(horizontalRules, verticalRules), new RuleSet(horizontalRules, verticalRules), elevationRules)));
    }
}
