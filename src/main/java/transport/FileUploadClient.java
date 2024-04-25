package transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.net.URI;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedFile;

public class FileUploadClient {
    public void uploadFile(File file, URI uri, String password) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // Create an SslContext for HTTPS if necessary.
            SslContext sslCtx = null;

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // Use HTTPS if necessary.
                            if (sslCtx != null) {
                                ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
                            }
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new FileUploadClientHandler(file));
                        }
                    });

            // Make the connection attempt.
            Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();

            // 设置请求参数
//            QueryStringEncoder encoder = new QueryStringEncoder(uri.toString());
//            encoder.addParam("password", password);
//            uri = new URI(encoder.toString());
            // Prepare the HTTP request.
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath() + "?password=" + password);
            HttpHeaders headers = request.headers();
            headers.set(HttpHeaderNames.HOST, uri.getHost() + ":" + uri.getPort());
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.BYTES);
            headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length());  // 设置Content-Length

            // Send the HTTP request.
            ch.write(request);

            // Send the content.
            ChannelFuture lastContentFuture = ch.writeAndFlush(new HttpChunkedInput(new ChunkedFile(file)));

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File("d:/model.txt");
        URI uri = new URI("http://localhost:9999/upLoadFile");
        FileUploadClient client = new FileUploadClient();
        client.uploadFile(file, uri, "123");
    }
}