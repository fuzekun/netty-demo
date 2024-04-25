package leetcode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.function.Predicate;

public class L2736 {
    /*
        采用离线的方法
        1. 使用TP进行搜集{nums1[i], i or -i} -i 代表着是询问
        2. 按照第一维排序,问题变成，从当前的子数组中，找到值nums2[j] > queries[idx][1]的最nums1[j] + nums2[j]大值
        3. 把nums2[i]当作坐标，nums1[i] + nums2[i]当作值，进行树状数组更新。
        每次找[1, queries[i][1]]的最大值
        需要对queries[i][1]和nums2[i]进行离散化处理

    */
class PR implements Comparable<PR>{
        public int x;
        public int idx;
        public PR(int x, int idx) {
            this.x = x;
            this.idx = idx;
        }
        // 按照第一维进行逆序排序，第二维进行逆序排序。这样数字一定在询问的前面。满足等于的情况
        @Override
        public int compareTo(PR o) {
            int flag = Integer.compare(o.x, this.x);
            if (flag == 0)
                return Integer.compare(o.idx, this.idx);
            return flag;
        }
    }
    private final int maxn = (int)1e6 + 5;
    private int[] c = new int[maxn];
    private int id = 0;
    HashMap<Integer, Integer> mp = new HashMap<>();
    private void divosor(int[] nums) {
        for (int x : nums)
            if (mp.containsKey(x))
                continue;
            else mp.put(x, ++id);
    }
    private void update(int x, int val) {
        for (int i = x; i < maxn; i += i & -i)
            c[i] = Math.max(c[i], val);
    }
    private int getMax(int x) {
        int maxv = -1;
        for (int i = x; i != 0; i -= i & -i)
            maxv = Math.max(c[i], maxv);
        return maxv;
    }
    public int[] maximumSumQueries(int[] nums1, int[] nums2, int[][] queries) {
        int n = nums1.length, m = queries.length;
        PR[] nums = new PR[m + n];
        for (int i = 0; i < n; i++) {
            nums[i] = new PR(nums1[i], i);
        }
        for (int i = n; i < n + m; i++) {
            nums[i] = new PR(queries[i][0], -(i + 1));
        }
        Arrays.sort(nums);
        // 逆序排序，取最大值
        int[] ans = new int[m];
        for (int i = 0; i < n + m; i++) {
            int idx = nums[i].idx;
            if (idx < 0) {
                // idx对应的是queries的下表
                idx = Math.abs(idx) - 1;
                int b = queries[i][1];

            } else {
            }
        }

        return ans;
    }
}
