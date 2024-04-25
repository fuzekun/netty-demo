package files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Comparator;
import java.util.TreeSet;

/*
*
*
* @description: 快速获取所有的HiBrain4.1的全部项目
* 1. 需要包含HiBrain4.1
* 2. 去掉所有空格
* 3. 使用'/'进行分割
* 4. 得到第一个
* 输出项目名称，按照名称排序，不区分大小写排序。
* */
public class getProjectName {

    public static void main(String[] args) throws Exception{
        String fileName = "data.txt";
        BufferedReader bf = new BufferedReader(new FileReader(new File(fileName)));
        String line ;
        int cnt = 0;
        // 排序不区分大小写
        TreeSet<String> set = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        while ((line = bf.readLine()) != null) {
            if (!checkBrain4_1(line)) continue;
            line = line.replaceAll(" ", "");
            String name = line.split("/")[1];
//            System.out.println(name);
            cnt++;
            set.add(name);
        }
        System.out.println("总共有" + cnt + "个项目");
        System.out.println("去重后有" + cnt + "个项目");
        bf.close();
        for (String s: set) {
            System.out.println(s);
        }
    }
    private static boolean checkBrain(String s) {
        if (s.contains("HiBrain") && !s.contains("4.1"))
            return true;
        return false;
    }
    private static boolean checkBrain4_1(String s) {
        if (s.contains("HiBrain4.1"))
            return true;
        return false;
    }
}
