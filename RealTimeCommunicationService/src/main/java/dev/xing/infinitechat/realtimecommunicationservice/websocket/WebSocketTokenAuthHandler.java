package dev.xing.infinitechat.realtimecommunicationservice.websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class WebSocketTokenAuthHandler extends ChannelInboundHandlerAdapter {

    private final StringRedisTemplate redisTemplate;

    private final DiscoveryClient discoveryClient;



    /**
       * @MethodName channelRead
       * @Description  获取用户的 uuid 和 token 信息
       * @param: ctx
       * @param: msg
       * @Date 2024/11/11 00:31
       */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            // 获取用户 id 和 token，并设置为当前通道的属性
            String userUuid = Optional.ofNullable(request.headers().get("userUuid")).map(CharSequence::toString).orElse("");
            String token = Optional.ofNullable(request.headers().get("token")).map(CharSequence::toString).orElse("");

            NettyUtils.setAttr(ctx.channel(), NettyUtils.TOKEN, token);
            NettyUtils.setAttr(ctx.channel(), NettyUtils.UID, userUuid);

            ctx.pipeline().remove(this);
            ctx.fireChannelRead(request);
        } else {
            // 如果不是 HTTP 请求，直接传递给下一个处理器
            ctx.fireChannelRead(msg);
        }
    }
}
