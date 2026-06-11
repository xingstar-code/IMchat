package dev.xing.infinitechat.messagingservice.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.messagingservice.common.ServiceException;
import dev.xing.infinitechat.messagingservice.mapper.RedPacketMapper;
import dev.xing.infinitechat.messagingservice.mapper.RedPacketReceiveMapper;
import dev.xing.infinitechat.messagingservice.mapper.UserMapper;
import dev.xing.infinitechat.messagingservice.model.dto.RedPacketResponse;
import dev.xing.infinitechat.messagingservice.model.dto.RedPacketUser;
import dev.xing.infinitechat.messagingservice.model.entity.RedPacket;
import dev.xing.infinitechat.messagingservice.model.entity.RedPacketReceive;
import dev.xing.infinitechat.messagingservice.model.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetRedPacketService extends ServiceImpl<RedPacketMapper, RedPacket> {

    @Autowired
    private RedPacketReceiveMapper redPacketReceiveMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 获取红包详细信息，包括领取记录
     *
     * @param redPacketId 红包ID
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @return 红包详情响应
     */
    public RedPacketResponse getRedPacketDetails(Long redPacketId, Integer pageNum, Integer pageSize) {
        RedPacket redPacket = this.getById(redPacketId);
        if (redPacket == null) {
            throw new ServiceException("红包不存在");
        }

        List<RedPacketReceive> receives = redPacketReceiveMapper.selectByRedPacketId(redPacketId, (pageNum - 1) * pageSize, pageSize);
        List<RedPacketUser> userList = convertToUserList(receives);

        User sender = userService.getById(redPacket.getSenderId());

        return new RedPacketResponse(userList, sender.getUserName(), sender.getAvatar(), redPacket.getRedPacketWrapperText(),
                redPacket.getRedPacketType(), redPacket.getTotalAmount(),redPacket.getTotalCount(),redPacket.getRemainingAmount(),redPacket.getRemainingCount(),redPacket.getStatus());
    }

    /**
     * 将领取记录转换为用户列表
     *
     * @param receives 红包领取记录
     * @return 用户列表
     */
    private List<RedPacketUser> convertToUserList(List<RedPacketReceive> receives) {
        List<RedPacketUser> userList = new ArrayList<>();
        for (RedPacketReceive receive : receives) {
            User user = userMapper.selectById(receive.getReceiverId());
            userList.add(new RedPacketUser(user.getUserName(), user.getAvatar(), String.valueOf(receive.getReceivedAt()), receive.getAmount()));
        }
        return userList;
    }
}
