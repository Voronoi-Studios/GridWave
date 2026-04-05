package com.png.GridWaveCore.AlgoNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class RuleSet {
    @Nonnull
    public static final BuilderCodec<RuleSet> CODEC = BuilderCodec.builder(RuleSet.class, RuleSet::new)
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

    public String[][] getRuleSets() {
        return new String[][]{n.split(","),n.split(e),n.split(s),n.split(w)};
    }
}
