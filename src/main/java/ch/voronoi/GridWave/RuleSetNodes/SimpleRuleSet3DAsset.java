package ch.voronoi.GridWave.RuleSetNodes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class SimpleRuleSet3DAsset extends RuleSetAsset {

    @Nonnull
    public static final BuilderCodec<SimpleRuleSet3DAsset> CODEC = BuilderCodec.builder(SimpleRuleSet3DAsset.class, SimpleRuleSet3DAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("RuleSet", RuleSet.CODEC3D), (op, val) -> op.ruleSet = val, op -> op.ruleSet)
            .add()
            .build();

    private RuleSet ruleSet;

    @Override
    public RuleSet.Combo build() {
        return new RuleSet.Combo(ruleSet, ruleSet);
    }
}
