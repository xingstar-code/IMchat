package dev.xing.infinitechat.realtimecommunicationservice.service.impl;

import dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys.ChannelUser;
import dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys.OnlineUserVo;
import dev.xing.infinitechat.realtimecommunicationservice.module.vo.sys.UserChannel;
import dev.xing.infinitechat.realtimecommunicationservice.service.SysService;
import dev.xing.infinitechat.realtimecommunicationservice.websocket.ChannelManager;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName SysServiceImpl
 * @Description 系统接口服务
 * @Date 2024/11/23 17:09
 */
@Service
public class SysServiceImpl implements SysService {
    @Override
    public OnlineUserVo getOnlineUser() {
        OnlineUserVo onlineUserVo = new OnlineUserVo();
        ArrayList<UserChannel> userChannels = new ArrayList<>();
        ArrayList<ChannelUser> channelUsers = new ArrayList<>();

        ConcurrentMap<String, Channel> userChannelMap = ChannelManager.getUserChannelMap();
        ConcurrentMap<Channel, String> channelUserMap = ChannelManager.getChannelUserMap();

        // 便利 userChannelMap 添加到 userChannels
        userChannelMap.forEach((uuid, channel)->{
            UserChannel userChannel = new UserChannel();

            userChannel.setUuid(uuid);
            userChannel.setChannel(channel);
            userChannel.setChannelID(channel.id().asShortText());

            userChannels.add(userChannel);
        });

        channelUserMap.forEach((channel, uuid)->{
            ChannelUser channelUser = new ChannelUser();

            channelUser.setChannel(channel);
            channelUser.setUuid(uuid);
            channelUser.setChannelID(channel.id().asShortText());

            channelUsers.add(channelUser);
        });

        onlineUserVo.setChannelUser(channelUsers);
        onlineUserVo.setUserChannel(userChannels);
        onlineUserVo.setChannelUserCount(channelUsers.size());
        onlineUserVo.setUserChannelCount(userChannels.size());

        return onlineUserVo;
    }
}
