package ch.voronoi.GridWave.RuleSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.*;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.ElevationRulesAsset;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.SimpleVerticalRulesAsset;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class SimpleRuleSetAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<SimpleRuleSetAsset> CODEC = BuilderCodec.builder(SimpleRuleSetAsset.class, SimpleRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("HorizontalRules", HorizontalRules.CODEC), (op, val) -> op.horizontalRules = val, op -> op.horizontalRules)
            .add()
            .append(new KeyedCodec<>("VerticalRules", SimpleVerticalRulesAsset.CODEC), (op, val) -> op.simpleVerticalRulesAsset = val, op -> op.simpleVerticalRulesAsset)
            .add()
            .append(new KeyedCodec<>("ElevationRules", ElevationRulesAsset.CODEC), (op, val) -> op.elevationRulesAsset = val, op -> op.elevationRulesAsset)
            .add()
            .build();

    private SimpleVerticalRulesAsset simpleVerticalRulesAsset;
    private ElevationRulesAsset elevationRulesAsset;

    private HorizontalRules horizontalRules;

    @Override
    public RuleCombo build() {
        return new RuleCombo(new RuleSet(horizontalRules, simpleVerticalRulesAsset.verticalRules), new RuleSet(horizontalRules, simpleVerticalRulesAsset.verticalRules), elevationRulesAsset.elevationRules);
    }
}
