package transport;



import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * 作者：ZeKun Fu
 * 创建日期：2024/1/23
 * 类说明：文件下载handler
 */
/*注 解表示是现成安全的，表示这个类可以被线程共享。开发人员保证了这个类现成安全 */
@Sharable
public class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    // 静态对象引用，绝对的线程安全。缓存用户口令，可以随机生成一个uuid，太麻烦了
    private static String password = "";
    private static final String filePath = "d:/data/password.txt";
    private static final String savePath = "d:/imgs";
//    private static final String filePath = "/root/data/password.txt";
//    private static final String savePath = "/root/data/imgs/";
    private static final Logger logger = LogManager.getLogger(FileServerHandler.class);
    // 读取文件中的密码
    static {
        File file = new File(filePath);
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            while ((line = in.readLine()) != null) {
                password += line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 目标文件夹不存在，就创建一个
    static {
        File destDir = new File(savePath);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
    }

    /*客户端读到数据以后，就会执行*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
            throws Exception {

        // 打印请求url
        System.out.println("ip:" + getClientIp(ctx) + " " + request.uri());
        // 用户认证
        auth(request);

        //下载任务处理
        if (request.uri().contains("/downFile")) {
            // 只支持get的http请求
            if (request.method() != HttpMethod.GET) {
                responseErrorMsg(ctx, "400:Bad Request");
                return ;
            }
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            List<String>pathValues = queryStringDecoder.parameters().get("path");
            // 参数 "路径" 不存在
            if(pathValues == null || pathValues.isEmpty()) {
                responseErrorMsg(ctx, "400:Bad Request, Parameter Path was needed");
                return ;
            }
            String path = pathValues.get(0);
            // 文件不存在
            if (!new File(path).exists()) {
                responseErrorMsg(ctx, "404: File was not found");
                return ;
            }
            responseExportFile(ctx, path);
        }
        //上传接口处理
        if (request.uri().contains("/upLoadFile")) {
            // 只支持post的http请求
            if (request.method() != HttpMethod.POST) {
                responseErrorMsg(ctx, "400:Bad Request");
                return ;
            }
            MultipartRequest MultipartBody = getMultipartBody(request);
            Map<String, FileUpload> fileUploads = MultipartBody.getFileUploads();
            // 将上传的文件进行移动，使用线程池处理文件的上传
            BlockingQueue<String> okFiles = new LinkedBlockingDeque<>();
            BlockingQueue<String> badFiles = new LinkedBlockingDeque<>();
            CountDownLatch latch = new CountDownLatch(fileUploads.size());
            // 使用线程池
            ExecutorService threadPool = new ThreadPoolExecutor(2, 10, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));
            for (String key : fileUploads.keySet()) {
                // 采用多线程处理每一个文件
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        FileUpload fileUpload = fileUploads.get(key);
                        try {
                            if (saveFile(key, fileUpload.getFile())) {
                                okFiles.add(key);
                            } else badFiles.add(key);
                            latch.countDown();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
//            System.out.println("线程到此");
            // 等待线程执行完毕
            latch.await();
//            System.out.println("线程并未阻塞");
            StringBuffer respMsg = new StringBuffer();
            // 所有文件处理相同
            if (okFiles.isEmpty()) {
                responseOKMsg(ctx, "所有文件上传失败!");
                return ;
            } else if (badFiles.isEmpty()) {
                responseOKMsg(ctx, "所有文件上传成功!");
                return ;
            }
            // 部分失败，部分成功!
            for (String s: okFiles) {
                respMsg.append(s).append(",");
            }
            respMsg.append(" 等文件上传成功!\n");
            for (String s : badFiles) {
                respMsg.append(s).append(",");
            }

            respMsg.append(" 等文件上传失败!\n");
            responseOKMsg(ctx, respMsg.toString());
        }
        // 服务端主动关闭连接
        ctx.close();
    }


    /*连接建立以后*/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer(
                "Hello Netty", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void responseOKMsg(ChannelHandlerContext ctx, String content) {
        // 创建对应的 FullHttpResponse 对象
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content.getBytes()));
        // 设置响应头信息
        response.headers().set("Content-Type", "text/plain");
        response.headers().set("Content-Length", response.content().readableBytes());
        ctx.write(response);
        //刷新缓冲区数据，文件结束标志符
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
    private static void responseErrorMsg(ChannelHandlerContext ctx, String content) {
        // 创建对应的 FullHttpResponse 对象
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST,
                Unpooled.wrappedBuffer(content.getBytes()));
        // 设置响应头信息
        response.headers().set("Content-Type", "text/plain");
        response.headers().set("Content-Length", response.content().readableBytes());
        ctx.write(response);
        //刷新缓冲区数据，文件结束标志符
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    /**
     * <p>
     * 返回下载内容
     * </p>
     *
     * @param ctx: 通道处理器
     * @param path: 文件路径
     */
    public static void responseExportFile(ChannelHandlerContext ctx, String path) {
        File file = new File(path);
        try {
            //随机读取文件
            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            long fileLength = raf.length();
            //定义response对象
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            //设置请求头部
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream; charset=UTF-8");
            response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\";");
            ctx.write(response);
            //设置事件通知对象
            ChannelFuture sendFileFuture = ctx
                    .write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                //文件传输完成执行监听器
                @Override
                public void operationComplete(ChannelProgressiveFuture future)
                        throws Exception {
//                    System.out.println("file {} transfer complete.");
                }

                //文件传输进度监听器
                @Override
                public void operationProgressed(ChannelProgressiveFuture future,
                                                long progress, long total) throws Exception {

                    if (total < 0) {
//                        System.out.println("file {} transfer progress: {}");
                    } else {
//                        System.out.println("file {} transfer progress: {}/{}");
                    }
                }
            });
            //刷新缓冲区数据，文件结束标志符
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能描述
     * <p>解析文件上传</p>
     *
     * @author DarkKing 2019/10/9 15:24
     * @params [ctx, httpDecode]
     */
    private static MultipartRequest getMultipartBody(FullHttpRequest request) {
        try {
            //创建HTTP对象工厂
            HttpDataFactory factory = new DefaultHttpDataFactory(true);
            //使用HTTP POST解码器
            HttpPostRequestDecoder httpDecoder = new HttpPostRequestDecoder(factory, request);
            httpDecoder.setDiscardThreshold(0);
            if (httpDecoder != null) {
                //获取HTTP请求对象
                final HttpContent chunk = (HttpContent) request;
                //加载对象到加吗器。
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    //自定义对象bean
                    MultipartRequest multipartRequest = new MultipartRequest();
                    //存放文件对象
                    Map<String, FileUpload> fileUploads = new HashMap<>();
                    //存放参数对象
                    JSONObject body = new JSONObject();
                    //通过迭代器获取HTTP的内容
                    java.util.List<InterfaceHttpData> InterfaceHttpDataList = httpDecoder.getBodyHttpDatas();
                    for (InterfaceHttpData data : InterfaceHttpDataList) {
                        //如果数据类型为文件类型，则保存到fileUploads对象中
                        if (data != null && InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                            FileUpload fileUpload = (FileUpload) data;
                            fileUploads.put(data.getName(), fileUpload);
                        }
                        //如果数据类型为参数类型，则保存到body对象中
                        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                            Attribute attribute = (Attribute) data;
                            body.put(attribute.getName(), attribute.getValue());
                        }
                    }
                    //存放文件信息
                    multipartRequest.setFileUploads(fileUploads);
                    //存放参数信息
                    multipartRequest.setParams(body);

                    return multipartRequest;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getClientIp(ChannelHandlerContext ctx) {
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    /**
     *
     * @param request
     * 用户认证是否成功
     * */
    private boolean auth(HttpRequest request) {
        // GET 请求，参数通常包含在 URL 中
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());

        List<String>inpass = queryStringDecoder.parameters().get("password");
        if (inpass == null || inpass.isEmpty() || !password.equals(inpass.get(0))) {
            return false;
        }
        return true;
    }
    /**
     * @param targetName: 上传的文件名称
     * @param sourceFile: 上传的文件
     * */
    private boolean saveFile(String targetName, File sourceFile) throws IOException {
        File destDir = new File(savePath);
        if (sourceFile.exists() && sourceFile.isFile() && destDir.exists() && destDir.isDirectory()) {
            File destFile = new File(destDir, targetName); // 在目标目录下创建目标文件
            if (sourceFile.renameTo(destFile)) {
//                System.out.println(targetName + "文件上传成功!");
                logger.info(targetName + "文件上传成功!");
                return true;
            } else {
                logger.info(targetName + "文件上传失败");
//                System.out.println(targetName + "文件上传失败");
            }
        } else {
            System.out.println("源文件不存在或者不是文件，目标目录不存在或者不是目录");
        }
        return false;
    }
    private void printFile(FileUpload upload) throws IOException{
        InputStream in = new FileInputStream(upload.getFile());
        BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        String content = bf.lines().collect(Collectors.joining("\n"));
        //打印文件
        System.out.println("content in file is : \n" + content);
    }
    /**
     * @param multipartRequest: 文件请求;
     * 对其他内容进行请求
     * */
    private void postOtherParamSolver(MultipartRequest multipartRequest) {
        //输出参数信息
        JSONObject params = multipartRequest.getParams();
        //输出文件信息
        System.out.println(JSONObject.toJSONString(params));
    }
}

