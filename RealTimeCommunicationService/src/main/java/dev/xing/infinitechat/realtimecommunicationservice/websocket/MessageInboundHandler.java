package dev.xing.infinitechat.realtimecommunicationservice.websocket;

import cn.hutool.json.JSONUtil;
import dev.xing.infinitechat.realtimecommunicationservice.exception.MessageTypeException;
import dev.xing.infinitechat.realtimecommunicationservice.module.entity.AckData;
import dev.xing.infinitechat.realtimecommunicationservice.module.entity.LogOutData;
import dev.xing.infinitechat.realtimecommunicationservice.module.entity.MessageDTO;
import dev.xing.infinitechat.realtimecommunicationservice.enums.ClientMessageTypeEnum;
import dev.xing.infinitechat.realtimecommunicationservice.enums.ConfigEnum;
import dev.xing.infinitechat.realtimecommunicationservice.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;



@Slf4j
@Sharable
public class MessageInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final StringRedisTemplate redisTemplate;

    /**
     * @MethodName MessageInboundHandler
     * @Description 构造函数
     * @param: redisTemplate
     * @Date 2024/11/23 16:33
     */
    public MessageInboundHandler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @MethodName channelRead0
     * @Description 在通道中读取到了数据
     * @param: ctx
     * @param: message
     * @Date 2024/11/23 16:32
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame message) throws Exception {
        MessageDTO messageDTO = JSONUtil.toBean(message.text(), MessageDTO.class);
        log.info("messageDTO:{}", messageDTO);

        ClientMessageTypeEnum messageType = ClientMessageTypeEnum.of(messageDTO.getType());
        switch (messageType){
            // 处理 ACK 消息
            case ACK:
                processACK(messageDTO);
                break;

            // 处理退出登录包
            case LOG_OUT:
                processLogOut(ctx, messageDTO);
                break;

            // 处理心跳包
            case HEART_BEAT:
                processHeartBeat(ctx, messageDTO);
                break;

            // 处理异常包
            default:
                processIllegal(messageDTO);
        }
    }


    /**
     * @MethodName processACK
     * @Description ACK 数据包
     * @param: msg
     * @Date 2024/11/23 16:33
     */
    private void processACK(MessageDTO msg){
        // 处理客户端成功返回的数据
        AckData ackData = JSONUtil.toBean(msg.getData().toString(), AckData.class);
        log.info("ackData:{}",ackData);
        log.info("推送消息成功！");
    }

    /**
     * @MethodName processLogOut
     * @Description 处理退出登录数据包
     * @param: ctx
     * @param: msg
     * @Date 2024/11/23 16:33
     */
    private void processLogOut(ChannelHandlerContext ctx, MessageDTO msg){
        LogOutData logOutData = JSONUtil.toBean(msg.getData().toString(), LogOutData.class);
        Integer userUuid = logOutData.getUserUuid();
        log.info("请求断开用户{}的连接...",userUuid);
        offline(ctx);
        log.info("断开连接成功！");
    }

    /**
     * @MethodName processHeartBeat
     * @Description 处理心跳包
     * @param: ctx
     * @param: msg
     * @Date 2024/11/23 16:33
     */
    private void processHeartBeat(ChannelHandlerContext ctx, MessageDTO msg){
        log.info("收到心跳包");
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setType(ClientMessageTypeEnum.HEART_BEAT.getCode());
        TextWebSocketFrame frame = new TextWebSocketFrame(JSONUtil.toJsonStr(messageDTO));
        ctx.channel().writeAndFlush(frame);
    }

    /**
     * @MethodName processIllegal
     * @Description 处理非法请求包
     * @param: msg
     * @Date 2024/11/23 16:33
     */
    private void processIllegal(MessageDTO msg){
        throw new MessageTypeException("不支持的消息格式！");
    }


    /**
     * @MethodName channelActive
     * @Description 激活长链接
     * @param: ctx
     * @Date 2024/11/23 16:33
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
//        String userId = ChannelManager.getUserByChannel(channel);
//        log.info("客户连接成功， 用户ID：{}",userId + "管道地址： " + channel.remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * @MethodName channelInactive
     * @Description 关闭长链接
     * @param: ctx
     * @Date 2024/11/23 16:34
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        offline(ctx);

        super.channelInactive(ctx);
    }

    /**
     * @MethodName offline
     * @Description 处理用户下线
     * @param: ctx
     * @Date 2024/11/23 16:34
     */
    public void offline(ChannelHandlerContext ctx){
        String userUuid = ChannelManager.getUserByChannel(ctx.channel());

        // 先移除映射关系
        try{
            ChannelManager.removeChannelUser(ctx.channel());
            if (userUuid != null){
                ChannelManager.removeUserChannel(userUuid);
                log.info("客户端关闭连接UserId：{}, 客户端地址为：{}",userUuid, ctx.channel().remoteAddress());
            }

        }catch (Exception e){
            log.error("处理退出登录异常", e);

        }finally {
            // 关闭通道
            if (ctx.channel() != null){
                ctx.channel().close();
            }

            // 通过 Redis 发布用户登出消息
            if (userUuid != null){
                redisTemplate.convertAndSend(ConfigEnum.REDIS_CONVERT_SEND.getValue(), ConfigEnum.NETTY_SERVER_HEAD.getValue() + userUuid);
            }
        }
    }

    /**
     * @MethodName userEventTriggered
     * @Description 处理用户触发的事件（心跳、握手）
     * @param: ctx
     * @param: evt
     * @Date 2024/11/23 16:34
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            switch (event.state()){
                case READER_IDLE:
                    log.error("读空闲超时，关闭连接...{}, 用户ID{}",ctx.channel().remoteAddress(), ChannelManager.getUserByChannel(ctx.channel()));
                    offline(ctx);
                    break;

                case WRITER_IDLE:
                    log.error("写空闲超时，关闭连接...{}",ctx.channel().remoteAddress());
                    offline(ctx);
                    break;

                case ALL_IDLE:
                    log.error("读读空闲超时，关闭连接...{}",ctx.channel().remoteAddress());
                    offline(ctx);
                    break;
            }

        }else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String token = NettyUtils.getAttr(ctx.channel(), NettyUtils.TOKEN);
            String userUuid = NettyUtils.getAttr(ctx.channel(), NettyUtils.UID);

            // 对 token 进行校验，不通过则直接进行关闭
            if (!validateToken(userUuid, token)){
                log.info("token invalid");
                ctx.close();
                return;
            }

            // 放入到 channelManager中，如果存在则先对管道进行关闭
            // 存在并发问题，后期需要进行改进
            Channel channel = ChannelManager.getChannelByUserId(userUuid);
            if (channel != null) {
                ChannelManager.removeUserChannel(userUuid);
                ChannelManager.removeChannelUser(channel);
                channel.close();
            }

            // 在将新的 channel 放入到其中
            ChannelManager.addUserChannel(userUuid, ctx.channel());
            ChannelManager.addChannelUser(userUuid, ctx.channel());
            log.info("客户连接成功， 用户ID：{}",userUuid + "管道地址： " + ctx.channel().remoteAddress());
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * @MethodName validateToken
     * @Description 校验token
     * @param: userUuid
     * @param: token
     * @return: boolean
     * @Date 2024/11/23 16:35
     */
    private boolean validateToken(String userUuid, String token) {
        // 实现具体的 Token 校验逻辑
        Claims claims = JwtUtil.parse(token);
        String userId = claims.getSubject();

        // 校验不通过则直接返回 false
        if (userId == null || !userId.equals(userUuid)) {
            return false;
        }

        String redisToken = redisTemplate.opsForValue().get(userId);
        return redisToken != null && redisToken.equals(token);
    }

    /**
     * @MethodName exceptionCaught
     * @Description 捕获异常
     * @param: ctx
     * @param: cause
     * @Date 2024/11/23 16:35
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录异常日志
        log.error("捕获到异常: ", cause);

        try {
            offline(ctx);

        }catch (Exception e){
            log.error("关闭管道异常", e);
        }
    }
}