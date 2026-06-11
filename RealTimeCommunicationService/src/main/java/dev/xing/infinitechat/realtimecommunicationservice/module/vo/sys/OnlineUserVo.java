package dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

/**
 * @ClassName OnlineUserVo
 * @Description OnlineUser 响应数据
 * @Date 2024/11/23 16:56
 */
@Data
@Accessors(chain = true)
public class OnlineUserVo implements Serializable {

    // userChannel 用户和管道对应信息
    public List<UserChannel> userChannel;

    // channelUser 管道和用户对应的信息
    public List<ChannelUser> channelUser;

    // userChannelCount 用户和管道对应信息数量
    public int userChannelCount;

    // channelUserCount 管道和用户对应的信息数量
    public int channelUserCount;
}
