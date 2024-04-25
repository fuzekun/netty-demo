import sun.dc.pr.PRError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * @author: Zekun Fu
 * @date: 2024/2/1 19:56
 * @Description:
 */
public class StringUtils {

    private static final String SPACE = "&nbsp";
    private static final String EDN = "&end";
    private static final String NEXTLINE = "\n";
    private static StringBuffer process(char[] s, int[] index, int flag) {
        StringBuffer ans = new StringBuffer("<ul>");
        while (index[0] < s.length) {
            if (index[0] == 0 || (s[index[0]] != '&' && (s[index[0] - 1] == '\n' || flag == 1))) {
                // 如果刚进来、刚回来，或者上一行结束，并且不是下一级的情况下
                ans.append("<li>");
                flag = 0;
            }
            if (s[index[0]] == '&') {
                if (index[0] < s.length && s[index[0] + 1] == 'n') {
                    index[0] += 5;
                    // 如果碰到下一级，递归处理
                    ans.append(process(s, index, 1));
                    flag = 1;
                } else {
                    // 碰见结束标志，返回上一级
                    index[0] += 3;
                    break;
                }
            } else if (s[index[0]] == '\n') {
                ans.append("</li>");
            } else ans.append(s[index[0]]);
            index[0]++;
        }
        ans.append("</ul>");
        return ans;
    }

    private static String processContent(String s) {
        int[] index = new int[1];
        return process(s.toCharArray(), index, 1).toString();
    }

    public static void test() {
        Hashtable<String, HashMap<String, List<String>>> mp = new Hashtable<>();
        
    }

    public static void main(String[] args) {
        String s = "数据源：ds1;\n&nbsp数据库：db1;\n&nbsp&nbsp数据表:t1;\n&nbsp&nbsp&nbsp数据列:\n&nbsp&nbsp&nbsp&nbspc1;\n&end&nbsp&nbsp&nbsp&nbspc2;\n&end&end&end&end&end&end" +
                "&nbsp数据库：db2;\n&nbsp&nbsp数据表:t2;\n&nbsp&nbsp&nbsp数据列:\n&nbsp&nbsp&nbsp&nbspc1;\n&nbsp&nbsp&nbsp&nbspc2;\n&end&end&end&end&end&end&end";
        s = "数据源: ds1;\n&nbsp数据库:db1;\n&nbsp数据表:t1;\n&nbsp数据列;\n&nbspc1;\nc2\n&end&end&end" +
                "数据库:db1;\n&nbsp数据表t1;\n&nbsp数据列;\n&nbspc1;\n&end&end表:t2;\n&nbsp数据列:\n" +
                "&nbspc3;\nc4;\n";
        String ans = processContent(s);
        System.out.println(ans);
    }
}
