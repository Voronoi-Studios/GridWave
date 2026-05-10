package ch.voronoi.GridWave.RuleSetNodes.Components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class HorizontalRules {
    @Nonnull
    public static final BuilderCodec<HorizontalRules> CODEC = BuilderCodec.builder(HorizontalRules.class, HorizontalRules::new)
            .append(new KeyedCodec<>("N", Codec.STRING, true), (t, n) -> t.n = n.replace(" ", "").split(","), t -> String.join(",", t.n))
            .add()
            .append(new KeyedCodec<>("E", Codec.STRING, true), (t, e) -> t.e = e.replace(" ", "").split(","), t -> String.join(",", t.e))
            .add()
            .append(new KeyedCodec<>("S", Codec.STRING, true), (t, s) -> t.s = s.replace(" ", "").split(","), t -> String.join(",", t.s))
            .add()
            .append(new KeyedCodec<>("W", Codec.STRING, true), (t, w) -> t.w = w.replace(" ", "").split(","), t -> String.join(",", t.w))
            .add()
            .build();

    private String[] n = new String[]{""};
    private String[] e = new String[]{""};
    private String[] s = new String[]{""};
    private String[] w = new String[]{""};

    public HorizontalRules() {
    }

    public HorizontalRules(String[][] keys) {
        if (keys.length != 4) return;
        this.n = keys[0];
        this.e = keys[1];
        this.s = keys[2];
        this.w = keys[3];
    }

    public HorizontalRules(String[] n, String[] e, String[] s, String[] w) {
        this.n = n;
        this.e = e;
        this.s = s;
        this.w = w;
    }

    public static HorizontalRules createSimpleFrom(String[] keys) {
        if (keys.length != 4) return new HorizontalRules();
        return new HorizontalRules(new String[]{keys[0]}, new String[]{keys[1]}, new String[]{keys[2]}, new String[]{keys[3]});
    }

    public String[][] getArrays() {
        return new String[][]{n, e, s, w};
    }

    public static final HorizontalRules EMPTY = HorizontalRules.createSimpleFrom(new String[]{"", "", "", ""});
    public static final HorizontalRules NULL = new HorizontalRules(new String[1], new String[1], new String[1], new String[1]);
    public static final HorizontalRules ALL_N = HorizontalRules.createSimpleFrom(new String[]{"N", "N", "N", "N"});
    public static final HorizontalRules ALL_X = HorizontalRules.createSimpleFrom(new String[]{"X", "X", "X", "X"});
}
