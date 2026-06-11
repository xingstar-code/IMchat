package dev.xing.infinitechat.realtimecommunicationservice.websocket;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChannelManager {

    // USER_CHANNEL_MAP 用户与管道之间的映射
    private static final ConcurrentMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    // CHANNEL_USER_MAP 管道与用户之间的映射
    private static final ConcurrentMap<Channel, String> CHANNEL_USER_MAP = new ConcurrentHashMap<>();

    /**
     * @MethodName getUserChannelMap
     * @Description 获取用户与管道的映射
     * @return: ConcurrentMap<String, Channel>
     * @Date 2024/11/23 17:21
     */
    public static ConcurrentMap<String, Channel> getUserChannelMap(){
        return USER_CHANNEL_MAP;
    }

    /**
     * @MethodName getChannelUserMap
     * @Description 获取管道与用户之间的映射
     * @return: ConcurrentMap<Channel, String>
     * @Date 2024/11/23 17:22
     */
    public static ConcurrentMap<Channel, String> getChannelUserMap(){
        return CHANNEL_USER_MAP;
    }

    /**
     * 添加用户与通道的映射
     *
     * @param userUuid 用户UUID
     * @param channel  Netty通道
     */
    public static void addUserChannel(String userUuid, Channel channel) {
        USER_CHANNEL_MAP.put(userUuid, channel);
    }

    public static void addChannelUser(String userUuid, Channel channel) {
        CHANNEL_USER_MAP.put(channel, userUuid);
    }

    /**
     * 移除用户与通道的映射
     *
     * @param userUuid 用户UUID
     */
    public static void removeUserChannel(String userUuid) {
        USER_CHANNEL_MAP.remove(userUuid);
    }

    public static void removeChannelUser(Channel channel){
        CHANNEL_USER_MAP.remove(channel);
    }

    /**
     * 根据用户UUID获取通道
     *
     * @param userUuid 用户UUID
     * @return Netty通道
     */
    public static Channel getChannelByUserId(String userUuid) {
        return USER_CHANNEL_MAP.get(userUuid);
    }

    /**
       * @MethodName getUserByChannel
       * @Description  通过 channel 获取用户 ID
       * @param: channel
       * @return: java.lang.String
       * @Date 2024/11/11 00:05
       */
    public static String getUserByChannel(Channel channel){
        return CHANNEL_USER_MAP.get(channel);
    }
    /**
     * 根据通道获取用户UUID
     *
     * @param channel Netty通道
     * @return 用户UUID
     */
    public static String getUserId(Channel channel) {
        // 遍历 map 查找对应的 userUuid
        System.out.println("Current user-channel mappings: " + USER_CHANNEL_MAP);
        for (Map.Entry<String, Channel> entry : USER_CHANNEL_MAP.entrySet()) {
            if (entry.getValue().equals(channel)) {
                return entry.getKey();
            }
        }
        // 如果未找到，返回 null 或者抛出异常

        return null;
    }
}
