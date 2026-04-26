package ch.voronoi.GridWave.RuleSetNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class SimpleRuleSetAsset extends RuleSetAsset{

    @Nonnull
    public static final BuilderCodec<SimpleRuleSetAsset> CODEC = BuilderCodec.builder(SimpleRuleSetAsset.class, SimpleRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSet.CODEC), (op, val) -> op.ruleSet = val, op -> op.ruleSet)
            .add()
            .build();

    private RuleSet ruleSet;

    @Override
    public RuleSet.Combo build() {
        return new RuleSet.Combo(ruleSet, ruleSet);
    }
}
