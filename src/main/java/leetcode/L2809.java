package leetcode;

public class L2809 {

    public int solve(int[] nums, int[] nums2, int x) {
        /*
        * 性质
        * 1. 每一个下标i最多需要改变一次，没必要改变两次。
        * 2. 改变应该是nums2小的优先
        *
        *
        * 做法dp
        * f[i][j]：表示下标i，在j时间内改变的最大值
        * 两种转移，要么选择i改变，要么不选择i不改变
        * f[i][j] = f[i - 1][j - 1], f[i - 1][j]
        * */
        return -1;
    }
    public static void main(String[] args) {
        int[] nums1 = {1,2,3}, nums2 = {1,2,3};
        int x = 4;
        int ans = new L2809().solve(nums1, nums2, x);
        System.out.println(ans);
    }
}
