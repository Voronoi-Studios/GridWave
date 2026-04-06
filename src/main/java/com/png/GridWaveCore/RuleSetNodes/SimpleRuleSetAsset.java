package com.png.GridWaveCore.RuleSetNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class SimpleRuleSetAsset extends RuleSetAsset{

    @Nonnull
    public static final BuilderCodec<SimpleRuleSetAsset> CODEC = BuilderCodec.builder(SimpleRuleSetAsset.class, SimpleRuleSetAsset::new, RuleSetAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n, t -> t.n)
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e, t -> t.e)
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s, t -> t.s)
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w, t -> t.w)
            .add()
            .build();

    private String n = "";
    private String e = "";
    private String s = "";
    private String w = "";

    @Override
    public RuleSet.Combo build() {
        return new RuleSet.Combo(new RuleSet(n,e,s,w), new RuleSet(n,e,s,w));
    }
}
