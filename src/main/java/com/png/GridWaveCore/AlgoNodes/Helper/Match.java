package com.png.GridWaveCore.AlgoNodes.Helper;

import com.png.GridWaveCore.RuleSetNodes.RuleSet;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class Match {
    static final int[] oppositeDirection = {2, 3, 0, 1};

    public static boolean dir(int dir, @Nonnull RuleSet.Combo a, @Nonnull RuleSet.Combo b){
        return dir(dir, a.providerRuleSet(), b.recieverRuleSet()) && dir(dir, a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean dir(int dir, @Nonnull RuleSet a, @Nonnull RuleSet b){
        return array(a.getRuleSetArrays()[oppositeDirection[dir]],b.getRuleSetArrays()[dir]);
    }

    public static boolean is(@Nonnull RuleSet.Combo a, @Nonnull RuleSet.Combo b){
        return is(a.providerRuleSet(), b.recieverRuleSet()) && is(a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean is(@Nonnull RuleSet a, @Nonnull RuleSet b){
        return is(a.getRuleSetArrays(),b.getRuleSetArrays());
    }
    public static boolean is(String[][] a, String[][] b){
        return a == null || b == null || a.length == b.length && IntStream.range(0, a.length).allMatch(i -> arrayIs(a[i], b[i]));
    }

    public static boolean arrayIs(@Nonnull String[] a, @Nonnull String[] b){
        for (String x : a) { for (String y : b) { if (x.equals(y)) return true; } } return false;
    }


    public static boolean full(@Nonnull RuleSet.Combo a, @Nonnull RuleSet.Combo b){
        return full(a.providerRuleSet(), b.recieverRuleSet()) && full(a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean full(@Nonnull RuleSet a, @Nonnull RuleSet b){
        return full(a.getRuleSetArrays(),b.getRuleSetArrays());
    }
    public static boolean full(String[][] a, String[][] b){
        return a == null || b == null || a.length == b.length && IntStream.range(0, a.length).allMatch(i -> array(a[i], b[i]));
    }

    public static boolean array(@Nonnull String[] a, @Nonnull String[] b){
        for (String x : a) { for (String y : b) { if (single(x, y)) return true; } } return false;
    }
    public static boolean single(String a, String b){
        return a == null || b == null || a.isEmpty() || b.isEmpty() || a.equals("N") || b.equals("N") || a.equals("X") || b.equals("X") || a.equals(b);
    }
}
