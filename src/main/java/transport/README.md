

# nettyHTTP文件服务器



## 核心思路

> 1. 客户端开发
>    - 界面开发
>    - 点击文件上传，或者拖拽上传
>    - 展示可下载文件列表，点击文件进行下载，或者拖拽下载
> 2. 服务端开发上传
>    - 监听8080端口
>    - 进行账号密码验证，直接使用随机字符串，放入到文件中，同时对账号密码进行缓存。
>    - 接收文件，保存到本地/data/imgs文件夹下面
>    - 展示/data/imgs文件夹下面的文件



- 开发

> 1. 环境搭建，下载jar包，手动安装，因为是离线环境，非离线直接加入依赖即可
>
> ```
> mvn install:install-file -Dfile=netty-all-4.1.97.Final.jar -DgroupId=io.netty -DartifactId=netty-all -Dversion=4.1.97.Final -Dpackaging=jar
> ```
>
> 2. 服务器编写：FileServer, FileServerHandler，pipeline，multipart
> 3. 客户端编写：编写上传界面，支持通过拖拽和选择文件进行上传，然后集成到typro中
> 4. [postman文件上传测试](https://blog.csdn.net/weixin_43611145/article/details/106815703/)



## 本项目的部署

1. 修改`FileServerHandler`中的密码文件路径和上传文件的保存路径
2. pom文件的配置参考如下，指定主类为fileServer
3. 使用maven打包
4. 将jar包放入到服务器上，`java -jar .jar`运行jar包
5. 本地使用JavaFileUploader，就可以将d:/data/imgs文件夹下的图片，上传到服务器上了
6. 对于下载，这里没有显示，只有知道服务器的具体路径才能进行下载。可以直接发送get请求，请求{path，路径名}作为参数
```java

    <groupId>cn.fuzekun.utils</groupId>
    <artifactId>transport-image</artifactId>
    <version>1.0-SNAPSHOT</version>
<!--maven依赖-->
    <build>
        <plugins>
<!--            指定主类-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>transport.JavaFileUploader</mainClass>
<!--                            <mainClass>transport.FileServer</mainClass>-->
<!--                            <mainClass>NIO.CommunityClient</mainClass>-->
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
<!--        指定不打的包-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <artifactSet>
                                <excludes>
                                    <exclude>files/**</exclude>
                                    <exclude>fileUpload/**</exclude>
                                    <exclude>leetcode</exclude>
                                    <exclude>NIO</exclude>
                                </excludes>
                            </artifactSet>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

```

## 进阶
- 增加功能

> - 权限验证
> - 日志



> - ip黑白名单
> - 安全验证，使用非对称加密，对每一次的password进行验证
> - **只要拦截了post表单的用户名和密码，我就可以登陆别人的服务器了。**所以，基本没人可以拦截你的get请求，所以可以放心使用明文传输密码
> - 大文件的断点续传，错误重传
> - 打开typro，使用TCP通信，长连接服务器，文件直接同步到服务器上



- 一些问题

> 既然是发送的http的请求和响应，为什么监听的端口不是443?
>
> - 因为，使用的是http协议进行报文传输，也就是说，发送的报文的格式是这个，也可以不发送这个格式的报文。直接进行通信，完全没有什么问题。
>
> 使用TCP发送文件有什么好处呢？
>
> - 双工通信，长连接
>
>   - 可以在发送的时候，接收内容
>   - 有心跳机制的加成，如果两个跨服务器的应用之间需要保持长连接，可以减少TCP三次握手和https四次握手的次数。
>
> - 从哪里体现出来使用的是什么协议的？
>
>   - 
>
>   - ```java
>     SimpleChannelInboundHandler<FullHttpRequest> // 表示这个channelhandler用来处理http请求
>     ```



