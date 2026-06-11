package dev.xing.infinitechat.realtimecommunicationservice.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NettyServer {

    // port 监听端口
    @Value("${netty.port}")
    private int port;

    // bossGroup boss 组
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    // workerGroup 工作组
    private EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    // redisTemplate redis 客户端
    private final StringRedisTemplate redisTemplate;

    // discoveryClient 服务发现客户端
    private final DiscoveryClient discoveryClient;

    /**
     * @MethodName start
     * @Description 启动
     * @Date 2024/11/23 16:24
     */
    @PostConstruct
    public void start() throws InterruptedException {
        run();
    }

    /**
     * @MethodName run
     * @Description 运行
     * @Date 2024/11/23 16:23
     */
    public void run() throws InterruptedException {
        // 服务器启动引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO)) // 为 bossGroup 添加 日志处理器
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 维护心跳链接, 线上心跳为读空闲5分钟（参考微信）
                        pipeline.addLast(new IdleStateHandler( 24 * 60 * 60, 0, 0));
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        pipeline.addLast(new WebSocketTokenAuthHandler(redisTemplate, discoveryClient));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/api/v1/chat/message"));
                        pipeline.addLast(new MessageInboundHandler(redisTemplate));

                    }

                });
        serverBootstrap.bind(port).sync();
    }

    /**
     * @MethodName destroy
     * @Description 退出程序时释放资源
     * @Date 2024/11/23 16:24
     */
    @PreDestroy
    public void destroy() {
        Future<?> future = bossGroup.shutdownGracefully();
        Future<?> future1 = workerGroup.shutdownGracefully();
        future.syncUninterruptibly();
        future1.syncUninterruptibly();
        log.info("关闭 ws server 成功");
    }
}