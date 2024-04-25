package transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.File;
import java.io.RandomAccessFile;

public class FileUploadClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    private File file;
    private RandomAccessFile raf;

    public FileUploadClientHandler(File file) throws Exception {
        this.file = file;
        this.raf = new RandomAccessFile(file, "r");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            if (httpContent.content().readableBytes() > 0) {
                // Process the content if needed
            }
            if (httpContent instanceof LastHttpContent) {
                // File upload is complete, do necessary cleanup
                raf.close();
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}