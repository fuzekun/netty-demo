package fileUpload;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.concurrent.EventExecutorGroup;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

    private HttpRequest request;
    private HttpPostRequestDecoder decoder;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (request.method().name().equals("POST")) {
                decoder = new HttpPostRequestDecoder(request);
            }
        }

        if (decoder != null && msg instanceof DefaultHttpContent) {
            DefaultHttpContent content = (DefaultHttpContent) msg;
            ByteBuf byteBuf = content.content();
            decoder.offer(new DefaultHttpContent(byteBuf));

            if (msg instanceof LastHttpContent) {
                LastHttpContent trailer = (LastHttpContent) msg;
                decoder.offer(trailer);

                while (decoder.hasNext()) {
                    InterfaceHttpData data = decoder.next();
                    if (data != null && InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                        Attribute attribute = (Attribute) data;
                        String name = attribute.getName();
                        String value = attribute.getValue();
                        // 处理表单字段数据

                    } else if (data != null && InterfaceHttpData.HttpDataType.FileUpload == data.getHttpDataType()) {
                        // 处理文件上传数据
                        HttpData fileUpload = (HttpData) data;
                        if (fileUpload.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                            // 可以从fileUpload中获取文件内容并进行处理
                            FileUpload file = (FileUpload) fileUpload;
                            System.out.println("fileName is" + file.getFile().getPath());
                            //获取文件流
                            InputStream in = new FileInputStream(file.getFile());
                            BufferedReader bf = new BufferedReader(new InputStreamReader(in));
                            String file_content = bf.lines().collect(Collectors.joining("\n"));
                            //打印文件
                            System.out.println("content is \n" + file_content);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}