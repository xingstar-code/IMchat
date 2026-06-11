package dev.xing.infinitechat.realtimecommunicationservice.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import dev.xing.infinitechat.realtimecommunicationservice.common.ServiceException;
import dev.xing.infinitechat.realtimecommunicationservice.exception.MessageTypeException;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.*;
import dev.xing.infinitechat.realtimecommunicationservice.module.dto.push.FriendApplicationNotification;
import dev.xing.infinitechat.realtimecommunicationservice.module.entity.*;
import dev.xing.infinitechat.realtimecommunicationservice.enums.MessageTypeEnum;
import dev.xing.infinitechat.realtimecommunicationservice.enums.PushTypeEnum;
import dev.xing.infinitechat.realtimecommunicationservice.websocket.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@SuppressWarnings({"all"})
public class NettyMessageService {

    /**
     * 发送不同类型的推送消息
     *
     * @param pushType PushTypeEnum 枚举值
     * @param data     推送数据对象
     */
    public void sendPush(PushTypeEnum pushType, Object data, String receiveUserUuid) {
        if (pushType == null || data == null || receiveUserUuid == null) {
            log.error("推送消息的类型、数据或接收用户UUID为空！");
            return;
        }

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setType(pushType.getCode());
        messageDTO.setData(data);

        Channel channel = ChannelManager.getChannelByUserId(receiveUserUuid);
        log.info("channel:{}", channel);
        if (channel != null && channel.isActive()) {
            log.info("准备发送消息，channel 状态: active={}, id={}, 发送内容: {}",
                    channel.isActive(),
                    channel.id(),
                    JSONUtil.toJsonStr(messageDTO));
            // 创建 WebSocket 帧
            TextWebSocketFrame frame = new TextWebSocketFrame(JSONUtil.toJsonStr(messageDTO));
            // 发送消息并添加监听器来处理发送结果
            channel.writeAndFlush(frame).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("消息发送成功: {}", messageDTO);
                    } else {
                        log.error("消息发送失败: {}", future.cause());
                    }
                }
            });
            //channel.writeAndFlush(frame).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            //log.info("发送推送消息: type={}, data={}", pushType, data);
        } else {
            // 处理用户离线或通道不可用的情况
            log.warn("用户 {} 的通道不可用或不活跃，推送消息失败。", receiveUserUuid);
            throw new ServiceException("用户" + receiveUserUuid + "的通道不可用或不活跃，推送消息失败。");
            // 您可以在这里将消息存储到离线消息队列或其他存储系统中
            // Todo: 存储到离线系统
        }
    }

    /**
     * 发送消息（原有功能）
     *
     * @param message 消息对象
     */
    public void sendMessageToUser(Message message) {
        switch (MessageTypeEnum.fromCode(message.getType())) {
            case TEXT_MESSAGE:
                TextMessage textMessage = new TextMessage();
                BeanUtils.copyProperties(message, textMessage);
                TextMessageBody textBean = BeanUtil.toBean(message.getBody(), TextMessageBody.class);
                textMessage.setBody(textBean);
                log.info("textMessage:{}", textMessage);
                List<Long> textReceiveUserIds = textMessage.getReceiveUserIds();
                textMessage.setReceiveUserIds(null);
                for (Long textReceiveUser : textReceiveUserIds) {
                    log.info("textReceiveUser:{}", textReceiveUser);
                    log.info("是否存在管道: {}", ChannelManager.getChannelByUserId(textReceiveUser.toString()));
                    if (ChannelManager.getChannelByUserId(textReceiveUser.toString()) != null) {
                        log.info("调用 sendPush: {}", textReceiveUser);
                        sendPush(PushTypeEnum.MESSAGE_NOTIFICATION, textMessage, textReceiveUser.toString());
                    }
                }
                break;
            case PICTURE_MESSAGE:
                PictureMessage pictureMessage = new PictureMessage();
                BeanUtils.copyProperties(message, pictureMessage);
                PictureMessageBody pictureBean = BeanUtil.toBean(message.getBody(), PictureMessageBody.class);
                pictureMessage.setBody(pictureBean);
                log.info("pictureMessage:{}", pictureMessage);
                List<Long> pictureReceiveUserIds = pictureMessage.getReceiveUserIds();
                for (Long pictureReceiveUser : pictureReceiveUserIds) {
                    if (ChannelManager.getChannelByUserId(pictureReceiveUser.toString()) != null) {
                        sendPush(PushTypeEnum.MESSAGE_NOTIFICATION, pictureMessage, pictureReceiveUser.toString());
                    }
                }
                break;
            case RED_PACKET_MESSAGE:
                RedPacketMessage redPacketMessage = new RedPacketMessage();
                BeanUtils.copyProperties(message, redPacketMessage);
                RedPacketMessageBody redPacketBean = BeanUtil.toBean(message.getBody(), RedPacketMessageBody.class);
                redPacketMessage.setBody(redPacketBean);
                log.info("redPacketMessage:{}", redPacketMessage);
                List<Long> redPacketReceiveUserIds = redPacketMessage.getReceiveUserIds();
                redPacketMessage.setReceiveUserIds(null); // 不发送给前端接受者字段
                for (Long redPacketReceiveUser : redPacketReceiveUserIds) {
                    if (ChannelManager.getChannelByUserId(redPacketReceiveUser.toString()) != null) {
                        sendPush(PushTypeEnum.MESSAGE_NOTIFICATION, redPacketMessage, redPacketReceiveUser.toString());
                    }
                }
                break;
            default:
                log.error("不支持的消息类型！");
                throw new MessageTypeException("不支持该种消息类型");
        }
    }

    /**
     * 发送新会话通知
     *
     * @param notification 新会话通知对象
     * @param userId       接收通知的用户ID
     */
    public void sendNewSessionNotification(NewSessionNotification notification, String userId) {
        sendPush(PushTypeEnum.NEW_SESSION_NOTIFICATION, notification, userId);
    }

    /**
     * 发送朋友圈通知
     *
     * @param momentNotification 朋友圈通知对象
     */
    public void sendNoticeMoment(MomentNotification momentNotification) {
        List<Long> userIds = momentNotification.getReceiveUserIds();
        for (Long userId : userIds) {
            if (ChannelManager.getChannelByUserId(userId.toString()) != null) {
                MomentNotification momentNotificationSelf = new MomentNotification();
                BeanUtils.copyProperties(momentNotification, momentNotificationSelf);
                momentNotificationSelf.setReceiveUserIds(null);
                sendPush(PushTypeEnum.MOMENT_NOTIFICATION, momentNotificationSelf, userId.toString());
            }
        }
    }

    /**
     * 发送好友申请通知
     *
     * @param notification 好友申请通知对象
     * @param userId       接收通知的用户ID
     */
    public void sendFriendApplicationNotification(FriendApplicationNotification notification, String userId) {
        sendPush(PushTypeEnum.FRIEND_APPLICATION_NOTIFICATION, notification, userId);
    }

    /**
     * 发送新群会话通知
     *
     * @param notification 新群会话通知对象
     * @param userId       接收通知的用户ID
     */
    public void sendNewGroupSessionNotification(NewGroupSessionNotification notification, String userId) {
        sendPush(PushTypeEnum.NEW_SESSION_NOTIFICATION, notification, userId);
    }
}
