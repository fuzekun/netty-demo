package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/*
*
* 服务端Server
* - 进行两个客户端进行匹配
- 客户端left上线，等待匹配right
- 客户端right上线，匹配成功
- 两者进行P TO P的通信，可以随便发送，随便接收信息
* */
public class CommunityServer {
    private Selector selector;
    // 保存了所有的通道，可以通过遍历进行发送，也可以直接使用map保存点对点的
    private List<SocketChannel>channels = new ArrayList<>();
    private Map<SelectionKey, SocketChannel> clientMap = new HashMap<>();
//    private Map<SelectionKey, SocketChannel> clientMap = new HashMap<>();

    public void initServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功!");

        while (true) {
            int readyChannels = selector.select();      // 阻塞在这里
            if (readyChannels == 0) continue;

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isAcceptable()) {
                    // 每接收一个新的连接，创建一个新的通道+buffer的组合进行处理
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
//                    clientMap.put(socketChannel.register(selector, SelectionKey.OP_READ), socketChannel);
                    channels.add(socketChannel);
                    System.out.println("客户端连接：" + socketChannel.getRemoteAddress());
                    toPerson(socketChannel, "请输入需要连接谁:(a,b,c)；群聊请输入：群聊-0，退出群聊输入：群聊-1");
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = channel.read(buffer);
                    if (read > 0) {
                        String message = new String(buffer.array(), "UTF-8").trim();
                        System.out.println("接收消息：" + message);
                        broadcast(channel, message);
                    }
                }
            }
        }
    }

    private void broadcast(SocketChannel sender, String message) throws IOException {
        for (SocketChannel client : channels) {
            if (client != sender) {
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
                client.write(buffer);
            }
        }
    }
    private void toPerson(SocketChannel client, String msg) throws  IOException{
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes("UTF-8"));
        client.write(buffer);
    }

    public static void main(String[] args) throws IOException {
        CommunityServer server = new CommunityServer();
        server.initServer();
    }
}