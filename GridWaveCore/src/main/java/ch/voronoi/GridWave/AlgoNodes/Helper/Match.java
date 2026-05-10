package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleSet;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class Match {
    public static final int[] oppositeDirection = {2, 3, 0, 1, 5, 4};

    public static boolean dir(int dir, @Nonnull RuleCombo a, @Nonnull RuleCombo b){
        return dir(dir, a.providerRuleSet(), b.recieverRuleSet()) && dir(dir, a.recieverRuleSet(),b.providerRuleSet());
    }
    public static boolean dir(int dir, @Nonnull RuleSet a, @Nonnull RuleSet b){
        if(dir < 4) return array(a.horizontalRules().getArrays()[oppositeDirection[dir]],b.horizontalRules().getArrays()[dir]);
        return array(a.verticalRules().getArrays()[oppositeDirection[dir]-4],b.verticalRules().getArrays()[dir-4]);
    }

    public static boolean is(@Nonnull RuleCombo a, @Nonnull RuleCombo b){
        return is(a.providerRuleSet(), b.recieverRuleSet()) && is(a.recieverRuleSet(),b.providerRuleSet()); //wrong!? Why both?
    }
    public static boolean is(@Nonnull RuleSet a, @Nonnull RuleSet b){
        return is(a.horizontalRules().getArrays(),b.horizontalRules().getArrays()) && (
                a.verticalRules() == null || b.verticalRules() == null || is(a.verticalRules().getArrays(), b.verticalRules().getArrays())
        );
    }
    public static boolean is(String[][] a, String[][] b){
        return a == null || b == null || a.length == b.length && IntStream.range(0, a.length).allMatch(i -> arrayIs(a[i], b[i]));
    }

    public static boolean arrayIs(@Nonnull String[] a, @Nonnull String[] b){
        for (String x : a) { for (String y : b) { if (x.equals(y)) return true; } } return false;
    }


    public static boolean full(@Nonnull RuleCombo a, @Nonnull RuleCombo b){
        return full(a.providerRuleSet(), b.recieverRuleSet()) && full(a.recieverRuleSet(),b.providerRuleSet()); //wrong!? Why both?
    }
    public static boolean full(@Nonnull RuleSet a, @Nonnull RuleSet b){
        return full(a.horizontalRules().getArrays(),b.horizontalRules().getArrays()) && (
                (a.verticalRules() == null && b.verticalRules() == null) ||
                (a.verticalRules() != null && is(a.verticalRules().getArrays(), b.verticalRules().getArrays()))
        );
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
