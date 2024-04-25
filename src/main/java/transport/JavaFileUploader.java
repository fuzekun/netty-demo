package transport;

import io.netty.handler.codec.http.HttpRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class JavaFileUploader {
    private static String filePath = "d:/data/imgs";
//    private static String ip = "192.168.10.128:9999";
//    private static String ip = "localhost:9999";
    private static String ip = "node2:9999";
    private static String password = "123456";
    private static String url = "http://" + ip + "/upLoadFile?password="+password;
    private static void upLoad(HttpURLConnection con) {


        // 开启一个新线程进行文件上传

        File dir = new File(filePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            String boundary = Long.toHexString(System.currentTimeMillis()); // 定义分隔线
            try (
                    OutputStream output = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);
            ) {
                for (File file : files) {
                    if (file.isFile()) {
                        // 上传文件
                        writer.append("--" + boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"" + file.getName()+ "\"; filename=\"" + file.getName() + "\"").append("\r\n");
                        writer.append("Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName())).append("\r\n");
                        writer.append("\r\n").flush();

                        try (FileInputStream inputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            output.flush();
                        }
                        writer.append("\r\n").flush();
                    }
                }
                writer.append("--" + boundary + "--").append("\r\n");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(url);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置POST请求
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        String boundary = Long.toHexString(System.currentTimeMillis()); // 定义分隔线
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary); // 设置请求头
        // 上传文件
        upLoad(con);
        // 打印响应
        printResponse(con);
        // 关闭连接
        con.disconnect();
    }
    private static void printResponse(HttpURLConnection con) throws IOException {
        // 获取响应
        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println(response.toString());
        }
    }
}
