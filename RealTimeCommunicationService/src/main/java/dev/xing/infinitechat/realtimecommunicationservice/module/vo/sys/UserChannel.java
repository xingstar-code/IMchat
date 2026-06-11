package dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys;

import io.netty.channel.Channel;
import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

/**
 * @ClassName UserChannel
 * @Description UserChannel 用户和管道的对应信息
 * @Date 2024/11/23 16:56
 */
@Data
@Accessors(chain = true)
public class UserChannel implements Serializable {

    // uuid 用户 uuid
    public String uuid;

    // channel 用户对应的管道
    public Channel channel;

    // channelID 管道ID
    public String channelID;
}
