package ch.voronoi.GridWave.RuleSetNodes;

import ch.voronoi.GridWave.RuleSetNodes.Components.*;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.ElevationRulesAsset;
import ch.voronoi.GridWave.RuleSetNodes.SubNodes.AdvancedVerticalRulesAsset;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class AdvancedRuleSetAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<AdvancedRuleSetAsset> CODEC = AssetBuilderCodec.builder(AdvancedRuleSetAsset.class, AdvancedRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("HorizontalRulesProvider", HorizontalRules.CODEC), (op, val) -> op.horizontalRulesRulesProvider = val, op -> op.horizontalRulesRulesProvider)
            .add()
            .append(new KeyedCodec<>("HorizontalRulesReceiver", HorizontalRules.CODEC), (op, val) -> op.horizontalRulesRulesReceiver = val, op -> op.horizontalRulesRulesReceiver)
            .add()
            .append(new KeyedCodec<>("VerticalRules", AdvancedVerticalRulesAsset.CODEC), (op, val) -> op.advancedVerticalRulesAsset = val, op -> op.advancedVerticalRulesAsset)
            .add()
            .append(new KeyedCodec<>("ElevationRules", ElevationRulesAsset.CODEC), (op, val) -> op.elevationRulesAsset = val, op -> op.elevationRulesAsset)
            .add()
            .build();

    private HorizontalRules horizontalRulesRulesProvider;
    private HorizontalRules horizontalRulesRulesReceiver;

    private AdvancedVerticalRulesAsset advancedVerticalRulesAsset;
    private ElevationRulesAsset elevationRulesAsset;

    @Override
    public RuleCombo build() {
        return new RuleCombo(new RuleSet(horizontalRulesRulesProvider, advancedVerticalRulesAsset.verticalRulesProvider), new RuleSet(horizontalRulesRulesReceiver, advancedVerticalRulesAsset.verticalRulesReceiver), elevationRulesAsset.elevationRules);
    }
}