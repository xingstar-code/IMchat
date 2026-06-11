package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.model.dto.GroupMemberDTO;
import dev.xing.infinitechat.ContactService.model.dto.GroupMembersResponse;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 群聊相关的服务实现类
 */
@Service
public class GetGroupMembersService extends ServiceImpl<SessionMapper, Session> {
    @Autowired
    private SessionMapper groupMapper;

    /**
     * 获取指定群聊会话内的所有成员
     *
     * @param sessionId 群聊会话ID
     * @return 群成员列表
     */
    public GroupMembersResponse getGroupMembers(Long sessionId) {

        // 参数校验
        if (sessionId == null || sessionId <= 0) {
            throw new ServiceException("无效的会话ID");
        }

        // 校验会话是否存在且为群聊
        Session session = groupMapper.selectSessionById(sessionId);
        if (session == null) {
            throw new ServiceException("会话不存在");
        }
        if (!isGroupChat(session)) {
            throw new ServiceException("指定的会话不是群聊");
        }

        // 查询群成员信息
        List<GroupMemberDTO> dtos = groupMapper.selectGroupMembers(sessionId);
        return new GroupMembersResponse(dtos, dtos.size());
    }

    /**
     * 判断会话类型是否为群聊
     *
     * @param session 会话对象
     * @return 是否为群聊
     */
    private boolean isGroupChat(Session session) {
        int GROUP_CHAT_TYPE = 2;
        return session.getType() == GROUP_CHAT_TYPE;
    }


}
