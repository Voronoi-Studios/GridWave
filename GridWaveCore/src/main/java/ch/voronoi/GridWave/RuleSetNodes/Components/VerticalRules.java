package ch.voronoi.GridWave.RuleSetNodes.Components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class VerticalRules {
    @Nonnull
    public static final BuilderCodec<VerticalRules> CODEC = BuilderCodec.builder(VerticalRules.class, VerticalRules::new)
            .append(new KeyedCodec<>("U", Codec.STRING, true), (t, n) -> t.u = n.replace(" ", "").split(","), t -> String.join(",", t.u))
            .add()
            .append(new KeyedCodec<>("D", Codec.STRING, true), (t, e) -> t.d = e.replace(" ", "").split(","), t -> String.join(",", t.d))
            .add()
            .build();

    private String[] u = new String[]{""};
    private String[] d = new String[]{""};

    public VerticalRules() {
    }

    public VerticalRules(String[][] keys) {
        if (keys.length != 2) return;
        this.u = keys[0];
        this.d = keys[1];
    }

    public VerticalRules(String[] u, String[] d) {
        this.u = u;
        this.d = d;
    }

    public static VerticalRules createSimpleFrom(String[] keys) {
        if (keys.length != 2) return new VerticalRules();
        return new VerticalRules(new String[]{keys[0]}, new String[]{keys[1]});
    }

    public String[][] getArrays() {
        return new String[][]{u, d};
    }

    public static final VerticalRules EMPTY = VerticalRules.createSimpleFrom(new String[]{"", ""});
    public static final VerticalRules NULL = new VerticalRules(new String[1], new String[1]);
    public static final VerticalRules ALL_N = VerticalRules.createSimpleFrom(new String[]{"N", "N"});
    public static final VerticalRules ALL_X = VerticalRules.createSimpleFrom(new String[]{"X", "X"});
}
