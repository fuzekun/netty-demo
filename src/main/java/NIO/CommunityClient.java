package NIO;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class CommunityClient {
    public void initClient() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 8888));

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        // 创建新线程进行监听用户操作
        new Thread(()-> {
            while (true) {
                Scanner reader = new Scanner(System.in);
                while (true) {
                    System.out.println("Put message send to others>");
                    // 这个使用Scanner是阻塞读，所以发送的消息会阻塞在这里，应该开启新线程监听读
                    String line = reader.nextLine();
                    if (line.equals("exit")) {
                        break;
                    }
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put(line.getBytes("UTF-8"));
                        buffer.flip();
                        socketChannel.write(buffer);
                        buffer.clear();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = channel.read(buffer);
                    if (read > 0) {
                        String message = new String(buffer.array(), "UTF-8").trim();
                        System.out.println("接收消息：" + message);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        CommunityClient client = new CommunityClient();
        client.initClient();
    }
}