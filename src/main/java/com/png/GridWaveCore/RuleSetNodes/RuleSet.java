package com.png.GridWaveCore.RuleSetNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public RuleSet(String[] keys){
        if (keys.length != 4) return;
        this.n = keys[0];
        this.e = keys[1];
        this.s = keys[2];
        this.w = keys[3];
    }

    public RuleSet(String n, String e, String s, String w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
    }

    public RuleSet(){}

    public String[][] getRuleSetArrays() {
        return new String[][]{n.split(","),e.split(","),s.split(","),w.split(",")};
    }
    public String[] getRuleSets() {
        return new String[]{
                n != null && !n.isEmpty() ? n.split(",", 2)[0] : "",
                e != null && !e.isEmpty() ? e.split(",", 2)[0] : "",
                s != null && !s.isEmpty() ? s.split(",", 2)[0] : "",
                w != null && !w.isEmpty() ? w.split(",", 2)[0] : ""
        };
    }

    public static final RuleSet EMPTY = new RuleSet("","","","");
    public static final RuleSet NULL = new RuleSet(null,null,null,null);
    public static final RuleSet ALL_N = new RuleSet("N","N","N","N");
    public static final RuleSet ALL_X = new RuleSet("X","X","X","X");


    public record Combo(RuleSet providerRuleSet, RuleSet recieverRuleSet) {
        public static final Combo EMPTY = new Combo(RuleSet.EMPTY,RuleSet.EMPTY);
        public static final Combo NULL = new Combo(RuleSet.NULL,RuleSet.NULL);
        public static final Combo ALL_N = new Combo(RuleSet.ALL_N, RuleSet.ALL_N);
        public static final Combo ALL_X = new Combo(RuleSet.ALL_X,RuleSet.ALL_X);

        public String[] getDebug() {
            String[][] providerRuleSet = this.providerRuleSet.getRuleSetArrays();
            String[][] receiverRuleSet = this.recieverRuleSet.getRuleSetArrays();

            return IntStream.range(0, 4)
                .mapToObj(i -> Stream.concat(
                        Arrays.stream(providerRuleSet[i])
                                .map(p -> Arrays.asList(receiverRuleSet[i]).contains(p) ? p : "<" + p),
                        Arrays.stream(receiverRuleSet[i])
                                .filter(r -> !Arrays.asList(providerRuleSet[i]).contains(r))
                                .map(r -> ">" + r)
                ).collect(Collectors.joining(",")))
                .toArray(String[]::new);
        }
    }
}
