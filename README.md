# Netty简介

## 基础功能



- TCP粘包：**解码器**进行解决，在Handler之前增加新的**解码器**。

  - 使用`LineBasedFrameDecoder + StringDecoder`进行**换行符**的编码解码。或者使用**特殊字符**作为换行符

  - 使用固定长度

  - 在头部增加长度字段

- 编码器：比Json编码更短，性能更高的编码器的使用：protobuf，Jbos Marshalling

- Netty支持各种协议

  - netty支持的**http**：轻量级，灵活。

  - netty支持**websocket**编程：`DefaultFileRegin(FileChannel, Pos, Count)`

    - http的大头，半双工，不支持服务推送，安全验证开销大，不支持长连接。

    - websocket：没有大头，双工，支持服务端推送，没有安全开销，ping/pong心跳长连接

  - netty支持**UDP**服务端和客户端开发

  - netty支持**TCP**进行文件传输，

  - netty支持基**于私有协议栈**进行开发

- netty以**面向对象**的方式封装了**NIO**编程

## 总结

> - netty是对NIO的面向对象的封装
> - 提供了基于各种协议开发的解决方案，比如提供了TCP粘包问题解决方案，http的长连接以及TCP和http文件传输的解决方案。
> - 核心是好用的编码和解码器




## NIO的解决方案

> - NIO的服务端，高性能非阻塞的客户端
>   - 进行两个客户端进行匹配
>   - 客户端left上线，等待匹配right
>   - 客户端right上线，匹配成功
>   - 两者进行P TO P的通信，可以随便发送，随便接收信息
> - NIO服务端实现思路
>   - 一个客户端创建一个Channel，创建一个writeBuffer，注册到selector中。

```java
// NIO的服务端
selector是否可以注册多个Channel呢？或者说多个Channel是否能够注册到同一个selector上呢？这样一个selector可以监听多个端口了。
经过实践，完全没有问题，一个selector可以监听多个端口，每一个端口可以有多个通道。也就是可以监听多个端口的多个客户端连接。
但是一般不这样做，为什么呢？因为每一个端口使用的协议一般是固定的，如果监听两个端口，可能需要两套处理端口监听的逻辑。
    
    总结： 一般一个端口服务，监听一个端口，一个端口处理多个客户端连接，也就是处理多个channel。
```



## Netty服务端模型

> - 一个服务一个ServerBootStrap，监听一个端口，使用一个EventLoopGroup对客户端进行处理，一个EventLoop含有多个线程，也就是Reactor的多线程实现，每一个线程含有一个Reactor，然后对于读写事件，交给pipline进行处理。
> - 一个客户端对应着一个Channnel
> - 一个Channel含有一个pipeline，用来定义事件的处理步骤
> - 一个pipeline含有多个handler，用来定义对事件的具体处理



## Netty客户端模型

- 需要对客户端保持心跳机制

