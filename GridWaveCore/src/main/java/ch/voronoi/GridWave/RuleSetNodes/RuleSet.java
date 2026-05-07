package ch.voronoi.GridWave.RuleSetNodes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RuleSet { //Should be record?
    @Nonnull
    public static final BuilderCodec<RuleSet> CODEC2D = BuilderCodec.builder(RuleSet.class, RuleSet::new)
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n.replace(" ", "").split(","), t -> String.join(",",t.n))
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e.replace(" ", "").split(","), t -> String.join(",",t.e))
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s.replace(" ", "").split(","), t -> String.join(",",t.s))
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w.replace(" ", "").split(","), t -> String.join(",",t.w))
            .add()
            .build();

    @Nonnull
    public static final BuilderCodec<RuleSet> CODEC3D = BuilderCodec.builder(RuleSet.class, RuleSet::new)
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n.replace(" ", "").split(","), t -> String.join(",",t.n))
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e.replace(" ", "").split(","), t -> String.join(",",t.e))
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s.replace(" ", "").split(","), t -> String.join(",",t.s))
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w.replace(" ", "").split(","), t -> String.join(",",t.w))
            .add()
            .append(new KeyedCodec<>("U", Codec.STRING, true), (t, u) -> t.u = u.replace(" ", "").split(","), t -> String.join(",",t.u))
            .add()
            .append(new KeyedCodec<>("D", Codec.STRING, true), (t, d) -> t.d = d.replace(" ", "").split(","), t -> String.join(",",t.d))
            .add()
            .build();

    private String[] n = new String[]{""};
    private String[] e = new String[]{""};
    private String[] s = new String[]{""};
    private String[] w = new String[]{""};
    private String[] u = new String[]{""};
    private String[] d = new String[]{""};

    public RuleSet(){}

    public RuleSet(String[][] keys){
        if (keys.length < 4) return;
        this.n = keys[0];
        this.e = keys[1];
        this.s = keys[2];
        this.w = keys[3];
        if (keys.length < 6) return;
        this.u = keys[4];
        this.d = keys[5];
    }

    public RuleSet(String[] n, String[] e, String[] s, String[] w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
    }
    public RuleSet(String[] n, String[] e, String[] s, String[] w, String[] u, String[] d) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
        this.u = u;
        this.d = d;
    }

    public static RuleSet createSimpleFrom(String[] keys){
        if (keys.length == 4) return new RuleSet(new String[]{keys[0]},new String[]{keys[1]},new String[]{keys[2]},new String[]{keys[3]});
        if (keys.length == 6) return new RuleSet(new String[]{keys[0]},new String[]{keys[1]},new String[]{keys[2]},new String[]{keys[3]},new String[]{keys[4]},new String[]{keys[5]});
        return new  RuleSet();
    }

    public String[][] getRuleSetArrays() {
        return new String[][]{n,e,s,w,u,d};
    }

    //TO-DO: Make this 3D Ready
    public static final RuleSet EMPTY = RuleSet.createSimpleFrom(new String[]{"","","",""});
    public static final RuleSet NULL = new RuleSet(new String[1],new String[1],new String[1],new String[1]);
    public static final RuleSet ALL_N = RuleSet.createSimpleFrom(new String[]{"N","N","N","N"});
    public static final RuleSet ALL_X = RuleSet.createSimpleFrom(new String[]{"X","X","X","X"});


    public record Combo(RuleSet providerRuleSet, RuleSet recieverRuleSet) {
        //TO-DO: Make this 3D Ready
        public static final Combo EMPTY = new Combo(RuleSet.EMPTY,RuleSet.EMPTY);
        public static final Combo NULL = new Combo(RuleSet.NULL,RuleSet.NULL);
        public static final Combo ALL_N = new Combo(RuleSet.ALL_N, RuleSet.ALL_N);
        public static final Combo ALL_X = new Combo(RuleSet.ALL_X,RuleSet.ALL_X);

        public String[] toStringArray() {
            String[][] providerRuleSet = this.providerRuleSet.getRuleSetArrays();
            String[][] receiverRuleSet = this.recieverRuleSet.getRuleSetArrays();

            return IntStream.range(0, 6)
                .mapToObj(i -> Stream.concat(
                        Arrays.stream(providerRuleSet[i])
                                .map(p -> Arrays.asList(receiverRuleSet[i]).contains(p) ? p : "<" + p),
                        Arrays.stream(receiverRuleSet[i])
                                .filter(r -> !Arrays.asList(providerRuleSet[i]).contains(r))
                                .map(r -> ">" + r)
                ).collect(Collectors.joining(",")))
                .toArray(String[]::new);
        }

        public static Combo fromStringArray(String[] arr) {
            String[][] providerRuleSet = new String[6][];
            String[][] receiverRuleSet = new String[6][];

            for (int i = 0; i < 6; i++) {
                String[] parts = arr[i].isEmpty() ? new String[0] : arr[i].split(",");

                List<String> provider = new ArrayList<>();
                List<String> receiver = new ArrayList<>();

                for (String s : parts) {
                    if (s.startsWith("<")) {
                        String val = s.substring(1);
                        provider.add(val);
                    } else if (s.startsWith(">")) {
                        String val = s.substring(1);
                        receiver.add(val);
                    } else {
                        provider.add(s);
                        receiver.add(s);
                    }
                }

                providerRuleSet[i] = provider.toArray(String[]::new);
                receiverRuleSet[i] = receiver.toArray(String[]::new);
            }

            return new Combo(new RuleSet(providerRuleSet), new RuleSet(receiverRuleSet));
        }
    }
}
