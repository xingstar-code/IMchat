package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.enums.FriendStatus;
import dev.xing.infinitechat.ContactService.enums.SessionType;
import dev.xing.infinitechat.ContactService.enums.UserRole;
import dev.xing.infinitechat.ContactService.enums.UserStatus;
import dev.xing.infinitechat.ContactService.mapper.FriendMapper;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.mapper.UserMapper;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.dto.InviteGroupRequest;
import dev.xing.infinitechat.ContactService.model.dto.InviteGroupResponse;
import dev.xing.infinitechat.ContactService.model.dto.push.NewGroupSessionNotification;
import dev.xing.infinitechat.ContactService.model.entity.Friend;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import dev.xing.infinitechat.ContactService.model.entity.User;
import dev.xing.infinitechat.ContactService.model.entity.UserSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
/**
 * 群聊邀请服务实现类
 */
@Slf4j
@Service
public class GroupService extends ServiceImpl<UserSessionMapper, UserSession> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private PushService pushService;

    /**
     * 处理群聊邀请逻辑
     *
     * @param request 群聊邀请请求参数
     * @return 邀请结果
     * @throws ServiceException 业务异常
     */
    @Transactional
    public InviteGroupResponse inviteGroup(InviteGroupRequest request) throws Exception {
        Long sessionId = request.getSessionId();
        Long inviterId = request.getInviterId();
        List<Long> inviteeIds = request.getInviteeIds();

        Session session = sessionMapper.selectById(sessionId);

        // 参数校验
        validateParameters(session, inviterId, inviteeIds);

        // 验证好友关系并获取非好友ID
        Set<Long> nonFriendIds = getNonFriendIds(inviterId, inviteeIds);

        // 获取已在群聊中的用户ID
        Set<Long> alreadyInGroupIds = getAlreadyInGroupIds(sessionId, inviteeIds);

        // 准备成功和失败列表
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        // 构建推送新群会话消息
        NewGroupSessionNotification notification = buildNewGroupSessionNotification(inviterId, sessionId, session);

        // 插入新成员并推送通知
        processInvitees(inviteeIds, alreadyInGroupIds, nonFriendIds, sessionId, successIds, failedIds, notification);

        return new InviteGroupResponse(successIds, failedIds);
    }

    /**
     * 校验请求参数的合法性
     *
     * @param session    群聊会话
     * @param inviterId  邀请者用户ID
     * @param inviteeIds 被邀请者用户ID列表
     * @throws ServiceException 参数校验失败时抛出异常
     */
    private void validateParameters(Session session, Long inviterId, List<Long> inviteeIds) throws ServiceException {
        // 校验群聊会话是否存在且类型为群聊
        if (session == null || session.getStatus() != UserStatus.NORMAL.getValue()
                || session.getType() != SessionType.GROUP.getValue()) {
            throw new ServiceException("群聊会话不存在或状态不正常");
        }

        // 校验邀请者是否存在且状态正常
        User inviter = userMapper.selectById(inviterId);
        if (inviter == null || inviter.getStatus() != UserStatus.NORMAL.getValue()) {
            throw new ServiceException("邀请者不存在或状态不正常");
        }

        // 校验邀请者在群聊中的角色是否为群主或管理员
        UserSession inviterSession = getUserSession(inviterId, session.getId());
        if (inviterSession == null) {
            throw new ServiceException("邀请者不在群聊中");
        }

        if (!isInviterHasPermission(inviterSession.getRole())) {
            throw new ServiceException("邀请者没有权限邀请用户加入群聊");
        }

        // 校验被邀请者列表不能为空
        if (inviteeIds == null || inviteeIds.isEmpty()) {
            throw new ServiceException("被邀请者列表不能为空");
        }
    }

    /**
     * 获取邀请者在群聊中的会话信息
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @return 用户会话信息
     */
    private UserSession getUserSession(Long userId, Long sessionId) {
        QueryWrapper<UserSession> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("session_id", sessionId)
                .eq("status", UserStatus.NORMAL.getValue());
        return userSessionMapper.selectOne(query);
    }

    /**
     * 判断邀请者是否具有邀请权限
     *
     * @param role 用户角色
     * @return 是否具有权限
     */
    private boolean isInviterHasPermission(int role) {
        return role == UserRole.GROUP_OWNER.getValue() || role == UserRole.GROUP_ADMIN.getValue();
    }

    /**
     * 获取非好友的用户ID集合
     *
     * @param inviterId  邀请者用户ID
     * @param inviteeIds 被邀请者用户ID列表
     * @return 非好友的用户ID集合
     */
    private Set<Long> getNonFriendIds(Long inviterId, List<Long> inviteeIds) {
        Set<Long> nonFriendIds = new HashSet<>();

        for (Long inviteeId : inviteeIds) {
            if (!isUserActive(inviteeId)) {
                nonFriendIds.add(inviteeId);
                log.info("被邀请者ID {} 不存在或状态不正常", inviteeId);
                continue;
            }

            if (!areFriends(inviterId, inviteeId)) {
                nonFriendIds.add(inviteeId);
                log.info("邀请者ID {} 与被邀请者ID {} 不是好友", inviterId, inviteeId);
            }
        }

        return nonFriendIds;
    }

    /**
     * 判断用户是否存在且状态正常
     *
     * @param userId 用户ID
     * @return 是否存在且正常
     */
    private boolean isUserActive(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getStatus() == UserStatus.NORMAL.getValue();
    }

    /**
     * 判断两用户是否为好友
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 是否为好友
     */
    private boolean areFriends(Long userId, Long friendId) {
        QueryWrapper<Friend> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("friend_id", friendId)
                .eq("status", FriendStatus.NORMAL.getValue());
        Friend friend = friendMapper.selectOne(query);
        return friend != null;
    }

    /**
     * 获取已在群聊中的用户ID集合
     *
     * @param sessionId  群聊会话ID
     * @param inviteeIds 被邀请者用户ID列表
     * @return 已经在群聊中的用户ID集合
     */
    private Set<Long> getAlreadyInGroupIds(Long sessionId, List<Long> inviteeIds) {
        QueryWrapper<UserSession> query = new QueryWrapper<>();
        query.select("user_id")
                .eq("session_id", sessionId)
                .in("user_id", inviteeIds)
                .eq("status", UserStatus.NORMAL.getValue());

        List<UserSession> existingMembers = userSessionMapper.selectList(query);
        Set<Long> alreadyInGroupIds = new HashSet<>();
        for (UserSession member : existingMembers) {
            alreadyInGroupIds.add(member.getUserId());
        }
        return alreadyInGroupIds;
    }

    /**
     * 构建新群会话的通知消息
     *
     * @param inviterId 邀请者ID
     * @param sessionId 会话ID
     * @param session   会话信息
     * @return 新群会话通知对象
     */
    private NewGroupSessionNotification buildNewGroupSessionNotification(Long inviterId, Long sessionId, Session session) {
        String inviterIdStr = String.valueOf(inviterId);
        String sessionIdStr = String.valueOf(sessionId);
        String sessionName = session.getName();
        String sessionAvatarUrl = "http://localhost:8080/img/avatar/IM_GROUP.jpg";

        return new NewGroupSessionNotification(
                inviterIdStr,
                sessionIdStr,
                SessionType.GROUP.getValue(),
                sessionName,
                sessionAvatarUrl
        );
    }

    /**
     * 处理被邀请者列表，插入新成员并推送通知
     *
     * @param inviteeIds         被邀请者ID列表
     * @param alreadyInGroupIds 已在群聊中的用户ID集合
     * @param nonFriendIds      非好友的用户ID集合
     * @param sessionId          会话ID
     * @param successIds         成功邀请的用户ID列表
     * @param failedIds          邀请失败的用户ID列表
     * @param notification       新群会话通知
     */
    private void processInvitees(List<Long> inviteeIds,
                                 Set<Long> alreadyInGroupIds,
                                 Set<Long> nonFriendIds,
                                 Long sessionId,
                                 List<Long> successIds,
                                 List<Long> failedIds,
                                 NewGroupSessionNotification notification) throws Exception {
        for (Long inviteeId : inviteeIds) {
            if (alreadyInGroupIds.contains(inviteeId)) {
                failedIds.add(inviteeId);
                log.info("用户ID {} 已在群聊中", inviteeId);
                continue;
            }

            if (nonFriendIds.contains(inviteeId)) {
                failedIds.add(inviteeId);
                log.info("用户ID {} 与邀请者非好友关系或状态不正常", inviteeId);
                continue;
            }

            // 插入用户会话关系
            UserSession userSession = createUserSession(inviteeId, sessionId);

            boolean inserted = this.save(userSession);
            if (inserted) {
                successIds.add(inviteeId);
                pushService.pushGroupNewSession(inviteeId, notification);
            } else {
                failedIds.add(inviteeId);
                log.error("插入用户ID {} 到群聊失败", inviteeId);
            }
        }
    }

    /**
     * 创建用户会话对象
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @return 用户会话对象
     */
    private UserSession createUserSession(Long userId, Long sessionId) {
        Date now = new Date();
        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setSessionId(sessionId);
        userSession.setRole(UserRole.GROUP_MEMBER.getValue());
        userSession.setStatus(UserStatus.NORMAL.getValue());
        userSession.setCreatedAt(now);
        userSession.setUpdatedAt(now);
        return userSession;
    }
}