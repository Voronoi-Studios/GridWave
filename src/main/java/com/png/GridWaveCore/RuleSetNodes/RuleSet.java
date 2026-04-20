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
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n.replace(" ", "").split(","), t -> String.join(",",t.n))
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e.replace(" ", "").split(","), t -> String.join(",",t.e))
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s.replace(" ", "").split(","), t -> String.join(",",t.s))
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w.replace(" ", "").split(","), t -> String.join(",",t.w))
            .add()
            .build();

    private String[] n = new String[]{""};
    private String[] e = new String[]{""};
    private String[] s = new String[]{""};
    private String[] w = new String[]{""};

    public RuleSet(){}

    public RuleSet(String[][] keys){
        if (keys.length != 4) return;
        this.n = keys[0];
        this.e = keys[1];
        this.s = keys[2];
        this.w = keys[3];
    }

    public RuleSet(String[] n, String[] e, String[] s, String[] w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
    }

    public static RuleSet createSimpleFrom(String[] keys){
        if (keys.length != 4) new RuleSet();
        return new RuleSet(new String[]{keys[0]},new String[]{keys[1]},new String[]{keys[2]},new String[]{keys[3]});
    }

    public String[][] getRuleSetArrays() {
        return new String[][]{n,e,s,w};
    }

    public static final RuleSet EMPTY = RuleSet.createSimpleFrom(new String[]{"","","",""});
    public static final RuleSet NULL = new RuleSet(new String[1],new String[1],new String[1],new String[1]);
    public static final RuleSet ALL_N = RuleSet.createSimpleFrom(new String[]{"N","N","N","N"});
    public static final RuleSet ALL_X = RuleSet.createSimpleFrom(new String[]{"X","X","X","X"});


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
