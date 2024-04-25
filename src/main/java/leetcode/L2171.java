package leetcode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class L2171 {

    public long minimumRemoval(int[] beans) {
        int n = beans.length;
        long total = Arrays.stream(beans).sum();

        return 0;
    }

    public static void main(String[] args) {
        String[] array = {"a", "b", "c", "a", "b", "c", "a", "b", "a"};

        Map<String, Long> counts = Arrays.stream(array)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        System.out.println(counts);

        Set<String> set = Arrays.stream(array).collect(Collectors.toSet());
        System.out.println(set);
    }
}
