package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.mapper.UserMapper;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.dto.GroupExitRequestDTO;
import dev.xing.infinitechat.ContactService.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 退出群聊服务实现类
 */
@Service
public class ExitGroupService extends ServiceImpl<UserSessionMapper, UserSession> {
    private static final String USER_NOT_FOUND_MESSAGE = "用户不存在或状态异常";
    private static final String USER_NOT_IN_GROUP_MESSAGE = "用户不在该群聊中";
    private static final String EXIT_GROUP_SUCCESS_MESSAGE = "成功退出群聊";
    private static final int USER_STATUS_NORMAL = 1;
    private static final int SESSION_TYPE_GROUP = 2;
    private static final int USER_SESSION_STATUS_NORMAL = 1;

    private final UserMapper userMapper;
    private final SessionMapper sessionMapper;
    private final UserSessionMapper userSessionMapper;

    @Autowired
    public ExitGroupService(UserMapper userMapper,
                            SessionMapper sessionMapper,
                            UserSessionMapper userSessionMapper) {
        this.userMapper = userMapper;
        this.sessionMapper = sessionMapper;
        this.userSessionMapper = userSessionMapper;
    }

    /**
     * 用户退出群聊的业务逻辑
     *
     * @param requestDTO 群聊退出请求DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean exitGroup(GroupExitRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long sessionId = requestDTO.getSessionId();

        // 参数校验
        validateUser(userId);
        validateSession(sessionId);
        validateUserInGroup(userId, sessionId);

        // 删除用户与群聊的关联记录
        return deleteUserSession(userId, sessionId);
    }

    /**
     * 校验用户是否存在且状态正常
     *
     * @param userId 用户ID
     */
    private void validateUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != USER_STATUS_NORMAL) {
            throw new ServiceException(USER_NOT_FOUND_MESSAGE);
        }
    }

    /**
     * 校验会话是否为群聊且存在
     *
     * @param sessionId 会话ID
     */
    private void validateSession(Long sessionId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null || session.getType() != SESSION_TYPE_GROUP || session.getStatus() != USER_SESSION_STATUS_NORMAL) {
            throw new ServiceException("会话不存在或不是群聊");
        }
    }

    /**
     * 校验用户是否在群聊中
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     */
    private void validateUserInGroup(Long userId, Long sessionId) {
        UserSession userSession = userSessionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserSession>()
                        .eq("user_id", userId)
                        .eq("session_id", sessionId)
                        .eq("status", USER_SESSION_STATUS_NORMAL)
        );
        if (userSession == null) {
            throw new ServiceException(USER_NOT_IN_GROUP_MESSAGE);
        }
    }

    /**
     * 删除用户与群聊的关联记录
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     */
    private boolean deleteUserSession(Long userId, Long sessionId) {
        int deletedRows = userSessionMapper.delete(
                new QueryWrapper<UserSession>()
                        .eq("user_id", userId)
                        .eq("session_id", sessionId)
                        .eq("status", USER_SESSION_STATUS_NORMAL)
        );
        if (deletedRows == 1) {
            return true;
        }
        if (deletedRows == 0) {
            throw new ServiceException("退出群聊失败，关联记录不存在");
        }
        return false;
    }
}
