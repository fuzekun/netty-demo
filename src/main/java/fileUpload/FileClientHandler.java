package fileUpload;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import java.io.File;
import java.io.FileInputStream;

public class FileClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final String FILE_PATH = "d:/model.txt"; // 你要上传的文件路径

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] fileContent = new byte[(int) file.length()];
                fis.read(fileContent);

                DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload", Unpooled.wrappedBuffer(fileContent));
                request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
                request.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

                ctx.writeAndFlush(request);
            }
        } else {
            System.out.println("File not found");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // 客户端不需要处理服务端返回的数据
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}