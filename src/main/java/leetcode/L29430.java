package leetcode;

import java.util.*;
import java.util.stream.Collectors;

/*
*
* heights = [6,4,8,5,2,7], queries = [[0,1],[0,3],[2,4],[3,4],[2,2]
*
*
* 1. 按照询问的第一维对queries进行逆序排序，
*   h = max(he[q[cur][1]], he[q[cur][0]])，逆序遍历
*   问题转成h < 字数组中heights的最小id，使用二维偏序进行解决。在[1 + h, maxv]中找最小的id
* 2. 对高度和高度+1进行逆序离散化处理，给最大值最小的编号，最小值最大的编号，转换成树状数组
* 3. 逆序遍历，每次碰到当前x >= i的，进行询问的求解，然后在更新树状数组，否则直接更新
*
* 1. 逆序排序
* 2. 逆序离散化处理，因为需要求的[h + 1, max]， 逆序之后就变成[1, -h-1]了。
* 3. 离散化，需要离散h + 1，因为需要用到
*
* */
public class L29430 {

    final int maxn = (int)1e5;
    int[] c = new int[maxn];
    void uptead(int x, int val) {
        for (int i = x; i < maxn; i += i & -i)
            c[i] = Math.min(c[i], val);
    }
    int getMin(int x) {
        int ans = Integer.MAX_VALUE;
        for (int i = x; i != 0; i -= i & -i)
            ans = Math.min(c[i], ans);
        return ans;
    }
    int idx = 0;
    HashMap<Integer, Integer>mp = new HashMap<>();
    int get(int x) {
        if (!mp.containsKey(x))
            mp.put(x, ++idx);
        return mp.get(x);
    }
    class PR implements Comparable<PR> {
        int x, idx;
        public PR(int x, int idx) {
            this.x = x;
            this.idx = idx;
        }
        // 按照第一维逆序排序，也就是倒着看下标
        @Override
        public int compareTo(PR o) {
            return Integer.compare(o.x, this.x);
        }
    }
    public int[] leftmostBuildingQueries(int[] heights, int[][] queries) {
        int n = heights.length;
        int m = queries.length;
        Arrays.fill(c, Integer.MAX_VALUE);
        List<PR> list = new ArrayList<>();
        int[] ans  = new int[m];
        for (int i = 0; i < m; i++) {
            Arrays.sort(queries[i]);
            if (queries[i][0] == queries[i][1] || heights[queries[i][0]] < heights[queries[i][1]]) {
                // 特殊情况，相等，或者右边的比坐标的高，那么最小id是右边
                ans[i] = queries[i][1];
            } else {
                list.add(new PR(queries[i][1], i));
            }
        }
        PR[] nums = list.toArray(new PR[list.size()]);
        Arrays.sort(nums);
        // 逆序离散化处理，最大值给最小的编号
        int[] hh = new int[n << 1];
        for (int i = 0; i < n; i++) {
            hh[i] = -heights[i];
            hh[i + n] = -(heights[i] + 1);
        }
        Arrays.sort(hh);
        for (int i = 0; i < n << 1; i++)
            get(hh[i]);
        int cur = 0;
        for (int i = n - 1; i >= 0; i--) {
            while (cur < list.size() && nums[cur].x >= i) {
                // 当前的下标在i的后面，那么不能更新当前的高度到树状数组
                int idx = nums[cur].idx;
                int h = get(-Math.max(heights[queries[idx][0]], heights[queries[idx][1]]) - 1);
                int minv = getMin(h);
                ans[idx] = minv == Integer.MAX_VALUE ? -1 : minv;
                cur++;
            }
            uptead(get(-heights[i]), i);
        }
        return ans;
    }

    public static void main(String[] args) {
        int[][] queries = {{0, 7}, {3, 5}, {5, 2}, {3, 0}, {1, 6}};
        int[] heights = {5,3,8,2,6,1,4,6};
        int[] ans = new L29430().leftmostBuildingQueries(heights, queries);
        System.out.println(Arrays.toString(ans));
    }
}
