package ch.voronoi.GridWave.RuleSetNodes.Components;

import javax.annotation.Nonnull;

public record RuleSet(HorizontalRules horizontalRules, VerticalRules verticalRules) {
    public RuleSet() { this(null, null); }
    public RuleSet(@Nonnull HorizontalRules horizontalRules) { this(horizontalRules, null); }

    //TO-DO: Make this 3D Ready
    public static final RuleSet H_EMPTY = new RuleSet(HorizontalRules.EMPTY, null);
    public static final RuleSet H_NULL = new RuleSet(HorizontalRules.NULL,null);
    public static final RuleSet H_ALL_N = new RuleSet(HorizontalRules.ALL_N, null);
    public static final RuleSet H_ALL_X = new RuleSet(HorizontalRules.ALL_X,null);

}
