package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.mapper.UserMapper;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.dto.KickGroupMembersRequest;
import dev.xing.infinitechat.ContactService.model.dto.KickGroupMembersResponse;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import dev.xing.infinitechat.ContactService.model.entity.User;
import dev.xing.infinitechat.ContactService.model.entity.UserSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群聊踢人业务逻辑
 */
@Service
public class KickGroupService extends ServiceImpl<UserSessionMapper, UserSession> {
    private final UserMapper userMapper;
    private final SessionMapper sessionMapper;
    private final UserSessionMapper userSessionMapper;

    // 用户状态常量
    private static final int USER_STATUS_NORMAL = 1;

    // 会话类型常量
    private static final int SESSION_TYPE_GROUP = 2;

    // 用户会话角色常量
    private static final int ROLE_OWNER = 1;
    private static final int ROLE_ADMIN = 2;
    private static final int ROLE_MEMBER = 3;

    // 用户会话状态常量
    private static final int USER_SESSION_STATUS_NORMAL = 1;

    @Autowired
    public KickGroupService(UserMapper userMapper, SessionMapper sessionMapper, UserSessionMapper userSessionMapper) {
        this.userMapper = userMapper;
        this.sessionMapper = sessionMapper;
        this.userSessionMapper = userSessionMapper;
    }


    /**
     * 踢出群聊成员。
     *
     * @param request 包含 sessionId、operatorId 和 memberIds 的请求体
     * @return 成功移出群聊的用户 ID 列表
     */
    @Transactional
    public KickGroupMembersResponse kickGroupMembers(KickGroupMembersRequest request) {
        String sessionId = request.getSessionId();
        Long operatorId = request.getOperatorId();
        List<Long> memberIds = request.getMemberIds();

        // 参数校验
        User operator = validateOperator(operatorId, sessionId);
        validateMemberIds(memberIds);

        // 获取操作者在群聊中的角色
        UserSession operatorSession = getUserSession(operatorId, sessionId);
        boolean isOwner = operatorSession.getRole() == ROLE_OWNER;
        boolean isAdmin = operatorSession.getRole() == ROLE_ADMIN;

        // 校验并收集需要踢出的成员
        List<UserSession> membersToKick = validateAndCollectMembers(sessionId, memberIds, isOwner, isAdmin);

        // 执行踢人操作
        List<String> successIds = performKick(membersToKick);

        return new KickGroupMembersResponse(successIds);
    }

    /**
     * 校验操作者的合法性。
     *
     * @param operatorId 操作者的 userId
     * @param sessionId  群聊的 sessionId
     * @return 操作者的 User 实体
     * @throws ServiceException 如果操作者不存在、状态异常或无权限操作
     */
    private User validateOperator(Long operatorId, String sessionId) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null || operator.getStatus() != USER_STATUS_NORMAL) {
            throw new ServiceException("操作者不存在或状态异常");
        }

        Session session = getSession(sessionId);

        // 确保会话类型为群聊
        if (session.getType() != SESSION_TYPE_GROUP) {
            throw new ServiceException("指定的会话不是群聊");
        }

        // 检查操作者在群聊中的角色
        UserSession operatorSession = getUserSession(operatorId, sessionId);
        if (operatorSession == null) {
            throw new ServiceException("操作者不在该群聊中");
        }

        if (operatorSession.getRole() != ROLE_OWNER && operatorSession.getRole() != ROLE_ADMIN) {
            throw new ServiceException("操作者不是群主或管理员");
        }

        return operator;
    }

    /**
     * 根据 sessionId 获取会话信息。
     *
     * @param sessionId 群聊的 sessionId
     * @return 会话实体
     * @throws ServiceException 如果会话不存在或已删除
     */
    private Session getSession(String sessionId) {
        Long sessionIdLong;
        try {
            sessionIdLong = Long.parseLong(sessionId);
        } catch (NumberFormatException e) {
            throw new ServiceException("无效的 sessionId 格式");
        }

        Session session = sessionMapper.selectById(sessionIdLong);
        if (session == null || session.getStatus() != 1) { // 假设 status=1 为正常
            throw new ServiceException("群聊会话不存在或已删除");
        }

        return session;
    }

    /**
     * 获取用户在特定会话中的 UserSession 信息。
     *
     * @param userId    用户 ID
     * @param sessionId 会话 ID
     * @return UserSession 实体
     */
    private UserSession getUserSession(Long userId, String sessionId) {
        Long sessionIdLong;
        try {
            sessionIdLong = Long.parseLong(sessionId);
        } catch (NumberFormatException e) {
            throw new ServiceException("无效的 sessionId 格式");
        }

        QueryWrapper<UserSession> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("session_id", sessionIdLong).eq("status", USER_SESSION_STATUS_NORMAL);

        return userSessionMapper.selectOne(query);
    }

    /**
     * 校验 memberIds 列表是否为空。
     *
     * @param memberIds 被踢出者的 userId 列表
     * @throws ServiceException 如果列表为空
     */
    private void validateMemberIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ServiceException("被踢出者列表不能为空");
        }
    }

    /**
     * 校验每个被踢出成员的合法性，并收集对应的 UserSession 实体。
     *
     * @param sessionId 群聊的 sessionId
     * @param memberIds 被踢出者的 userId 列表
     * @param isOwner   操作者是否为群主
     * @param isAdmin   操作者是否为管理员
     * @return 需要踢出的成员的 UserSession 列表
     * @throws ServiceException 如果存在不合法的被踢出成员
     */
    private List<UserSession> validateAndCollectMembers(String sessionId, List<Long> memberIds, boolean isOwner, boolean isAdmin) {
        Long sessionIdLong = Long.parseLong(sessionId);

        // 批量查询被踢出成员的 UserSession
        QueryWrapper<UserSession> query = new QueryWrapper<>();
        query.eq("session_id", sessionIdLong).in("user_id", memberIds).eq("status", USER_SESSION_STATUS_NORMAL);

        List<UserSession> userSessions = userSessionMapper.selectList(query);

        // 检查所有 memberIds 是否都在群聊中
        Set<Long> existingMemberIds = userSessions.stream().map(UserSession::getUserId).collect(Collectors.toSet());
        List<Long> notInGroupIds = memberIds.stream().filter(id -> !existingMemberIds.contains(id)).collect(Collectors.toList());

        if (!notInGroupIds.isEmpty()) {
            throw new ServiceException("部分被踢出者不在该群聊中: " + notInGroupIds);
        }

        // 校验每个被踢出成员的角色
        for (UserSession userSession : userSessions) {
            int role = userSession.getRole();
            if (role == ROLE_OWNER) {
                throw new ServiceException("不能踢出群主: userId=" + userSession.getUserId());
            }
            if (isAdmin && role == ROLE_ADMIN) {
                throw new ServiceException("管理员不能踢出其他管理员: userId=" + userSession.getUserId());
            }
        }

        return userSessions;
    }

    /**
     * 执行踢人操作，通过更新 UserSession 状态。
     *
     * @param membersToKick 需要踢出的成员的 UserSession 列表
     * @return 成功移出的用户 ID 列表
     * @throws ServiceException 如果没有任何用户被成功踢出
     */
    private List<String> performKick(List<UserSession> membersToKick) {
        List<String> kickedUserIds = new ArrayList<>();

        for (UserSession userSession : membersToKick) {
/*            userSession.setStatus(2); // 设置状态为已删除
            int update = userSessionMapper.updateById(userSession);
            if (update > 0) {
                kickedUserIds.add(userSession.getUserId());
            }*/

            int delete = userSessionMapper.deleteById(userSession.getId());
            if (delete > 0) {
                kickedUserIds.add(String.valueOf(userSession.getUserId()));
            }
        }

        if (kickedUserIds.isEmpty()) {
            throw new ServiceException("没有成功踢出任何用户");
        }

        return kickedUserIds;
    }


}

