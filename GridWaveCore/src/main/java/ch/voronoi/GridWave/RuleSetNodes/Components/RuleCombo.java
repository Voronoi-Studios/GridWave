package ch.voronoi.GridWave.RuleSetNodes.Components;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record RuleCombo(@Nonnull RuleSet providerRuleSet, @Nonnull RuleSet recieverRuleSet, ElevationRules elevationRules) {
    //TO-DO: Make this 3D Ready
    public static final RuleCombo H_EMPTY = new RuleCombo(RuleSet.H_EMPTY, RuleSet.H_EMPTY, null);
    public static final RuleCombo H_NULL = new RuleCombo(RuleSet.H_NULL, RuleSet.H_NULL, null);
    public static final RuleCombo H_ALL_N = new RuleCombo(RuleSet.H_ALL_N, RuleSet.H_ALL_N, null);
    public static final RuleCombo H_ALL_X = new RuleCombo(RuleSet.H_ALL_X, RuleSet.H_ALL_X, null);

    public String[] toHorizontalStringArray() {
        String[][] horizontalProviderRuleSet = this.providerRuleSet.horizontalRules().getArrays();
        String[][] horizontalReceiverRuleSet = this.recieverRuleSet.horizontalRules().getArrays();

        return IntStream.range(0, 4)
                .mapToObj(i -> Stream.concat(
                        Arrays.stream(horizontalProviderRuleSet[i])
                                .map(p -> Arrays.asList(horizontalReceiverRuleSet[i]).contains(p) ? p : "<" + p),
                        Arrays.stream(horizontalReceiverRuleSet[i])
                                .filter(r -> !Arrays.asList(horizontalProviderRuleSet[i]).contains(r))
                                .map(r -> ">" + r)
                ).collect(Collectors.joining(",")))
                .toArray(String[]::new);
    }

    public static RuleCombo fromHorizontalStringArray(String[] arr) {
        String[][] horizontalProviderRuleSet = new String[4][];
        String[][] horizontalReceiverRuleSet = new String[4][];

        for (int i = 0; i < 4; i++) {
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

            horizontalProviderRuleSet[i] = provider.toArray(String[]::new);
            horizontalReceiverRuleSet[i] = receiver.toArray(String[]::new);
        }

        return new RuleCombo(new RuleSet(new HorizontalRules(horizontalProviderRuleSet)), new RuleSet(new HorizontalRules(horizontalReceiverRuleSet)), null);
    }
}
