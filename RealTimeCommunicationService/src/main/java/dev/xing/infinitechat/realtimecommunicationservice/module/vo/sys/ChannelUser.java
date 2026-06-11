package dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys;

import io.netty.channel.Channel;
import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

/**
 * @ClassName ChannelUser
 * @Description ChannelUser 管道和用户的对应信息
 * @Date 2024/11/23 16:56
 */
@Data
@Accessors(chain = true)
public class ChannelUser implements Serializable {

    // uuid 用户 uuid
    public String uuid;

    // channel 用户对应的管道
    public Channel channel;

    // channelID 管道 ID
    public String channelID;
}
