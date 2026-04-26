package ch.voronoi.GridWave.RuleSetNodes;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class AdvancedRuleSetAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<AdvancedRuleSetAsset> CODEC = AssetBuilderCodec.builder(AdvancedRuleSetAsset.class, AdvancedRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("Provider RuleSets", RuleSet.CODEC), (op, val) -> op.providerRuleSet = val, op -> op.providerRuleSet)
            .add()
            .append(new KeyedCodec<>("Receiver RuleSets", RuleSet.CODEC), (op, val) -> op.recieverRuleSet = val, op -> op.recieverRuleSet)
            .add()
            .build();

    private RuleSet providerRuleSet;
    private RuleSet recieverRuleSet;

    @Override
    public RuleSet.Combo build() {
        return new RuleSet.Combo(providerRuleSet, recieverRuleSet);
    }
}