package ch.voronoi.GridWave.RuleSetNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class SimpleRuleSet2DAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<SimpleRuleSet2DAsset> CODEC = BuilderCodec.builder(SimpleRuleSet2DAsset.class, SimpleRuleSet2DAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSet.CODEC2D), (op, val) -> op.ruleSet = val, op -> op.ruleSet)
            .add()
            .build();

    private RuleSet ruleSet;

    @Override
    public RuleSet.Combo build() {
        return new RuleSet.Combo(ruleSet, ruleSet);
    }
}
