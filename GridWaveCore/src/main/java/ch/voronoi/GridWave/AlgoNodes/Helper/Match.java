package ch.voronoi.GridWave.AlgoNodes.Helper;

import ch.voronoi.GridWave.RuleSetNodes.Components.RuleCombo;
import ch.voronoi.GridWave.RuleSetNodes.Components.RuleSet;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Match {
    public static final int[] oppositeDirection = {2, 3, 0, 1, 5, 4};

    public static boolean dir(int dir, @Nonnull RuleCombo a, @Nonnull RuleCombo b){
        return dir(dir, a.providerRuleSet(), b.recieverRuleSet()) && dir(dir, a.recieverRuleSet(),b.providerRuleSet());
    }
    private static boolean dir(int dir, @Nonnull RuleSet a, @Nonnull RuleSet b){
        if(dir < 4) return array(a.horizontalRules().getArrays()[oppositeDirection[dir]],b.horizontalRules().getArrays()[dir]);
        return array(a.verticalRules().getArrays()[oppositeDirection[dir]-4],b.verticalRules().getArrays()[dir-4]);
    }
    private static boolean array(@Nonnull String[] a, @Nonnull String[] b){
        for (String x : a) { for (String y : b) { if (single(x, y)) return true; } } return false;
    }
    private static boolean single(String a, String b){
        return a == null || b == null || a.isEmpty() || b.isEmpty() || a.equals("N") || b.equals("N") || a.equals("X") || b.equals("X") || a.equals(b);
    }

    public static boolean fancyMatch(@Nonnull RuleCombo fancyTile, @Nonnull RuleCombo baseTile){
        return fancyMatch(fancyTile.recieverRuleSet(), baseTile.providerRuleSet());
    }
    private static boolean fancyMatch(@Nonnull RuleSet f, @Nonnull RuleSet b){
        return fancyMatch(f.horizontalRules().getArrays(),b.horizontalRules().getArrays()) && (
                (f.verticalRules() == null && b.verticalRules() == null) ||
                (f.verticalRules() != null && fancyMatch(f.verticalRules().getArrays(), b.verticalRules().getArrays()))
        );
    }
    private static boolean fancyMatch(String[][] f, String[][] b){
        return f == null || b == null || f.length == b.length && IntStream.range(0, f.length).allMatch(i -> arrayContains(f[i], b[i]));
    }
    private static boolean arrayContains(@Nonnull String[] f, @Nonnull String[] b){
        for (String fPart : f) {
            if (fPart.isEmpty() || fPart.equals("N") || fPart.equals("X") || Arrays.stream(b).toList().contains(fPart)) return true;
        }
        return false;
    }
}
