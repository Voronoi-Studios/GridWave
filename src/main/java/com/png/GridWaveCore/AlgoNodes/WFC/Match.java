package com.png.GridWaveCore.AlgoNodes.WFC;

import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class Match {
    static final int[] oppositeDirection = {2, 3, 0, 1};

    public static boolean dir(int dir, @Nonnull RuleSet.Combo a, @Nonnull RuleSet.Combo b){
        return dir(dir, a.providerRuleSet(), b.recieverRuleSet()) && dir(dir, a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean dir(int dir, @Nonnull RuleSet a, @Nonnull RuleSet b){
        return full(a.getRuleSets()[oppositeDirection[dir]],b.getRuleSets()[dir]);
    }

    public static boolean full(@Nonnull RuleSet.Combo a, @Nonnull RuleSet.Combo b){
        return full(a.providerRuleSet(), b.recieverRuleSet()) && full(a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean full(@Nonnull RuleSet a, @Nonnull RuleSet b){
        return full(a.getRuleSets(),b.getRuleSets());
    }
    public static boolean full(String[] a, String[] b){
        return a == null || b == null || a.length == b.length && IntStream.range(0, a.length).allMatch(i -> full(a[i], b[i]));
    }
    public static boolean full(String a, String b){
        return a == null || b == null || a.isEmpty() || b.isEmpty() || a.equals("N") || b.equals("N") || a.equals("X") || b.equals("X") || a.equals(b);
    }
}
