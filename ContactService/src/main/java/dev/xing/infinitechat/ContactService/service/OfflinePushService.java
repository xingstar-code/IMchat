package dev.xing.infinitechat.ContactService.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.ContactService.mapper.*;
import dev.xing.infinitechat.ContactService.model.dto.push.FriendApplicationNotification;
import dev.xing.infinitechat.ContactService.model.dto.push.NewGroupSessionNotification;
import dev.xing.infinitechat.ContactService.model.dto.push.NewSessionNotification;
import dev.xing.infinitechat.ContactService.model.dto.push.OfflinePushResponse;
import dev.xing.infinitechat.ContactService.model.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 离线消息推送服务接口实现
 */
@Service
public class OfflinePushService extends ServiceImpl<UserMapper, User> {


    private static final int SESSION_TYPE_SINGLE = 1;
    private static final int SESSION_TYPE_GROUP = 2;
    private static final int FRIEND_REQUEST_TYPE = 4;

    private final FriendMapper friendMapper;
    private final SessionMapper sessionMapper;
    private final UserSessionMapper userSessionMapper;
    private final ApplyFriendMapper applyFriendMapper;

    @Autowired
    public OfflinePushService(FriendMapper friendMapper,
                              SessionMapper sessionMapper,
                              UserSessionMapper userSessionMapper,
                              ApplyFriendMapper applyFriendMapper) {
        this.friendMapper = friendMapper;
        this.sessionMapper = sessionMapper;
        this.userSessionMapper = userSessionMapper;
        this.applyFriendMapper = applyFriendMapper;
    }

    /**
     * 获取离线推送消息
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间
     * @return 离线推送消息响应
     */
    public OfflinePushResponse getOfflinePush(Long userId, LocalDateTime offlineTime) {
        OfflinePushResponse response = new OfflinePushResponse();

        // 获取新会话推送
        List<NewSessionNotification> newSessionPushes = getNewSessionPushes(userId, offlineTime);
        response.setNewSessionPushes(newSessionPushes);

        // 获取新群聊推送
        List<NewGroupSessionNotification> newGroupPushes = getNewGroupPushes(userId, offlineTime);
        response.setNewGroupPushes(newGroupPushes);

        // 获取好友申请推送
        List<FriendApplicationNotification> friendRequests = getFriendRequests(userId, offlineTime);
        response.setFriendRequests(friendRequests);

        return response;
    }

    /**
     * 获取新会话推送
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间
     * @return 新会话推送列表
     */
    private List<NewSessionNotification> getNewSessionPushes(Long userId, LocalDateTime offlineTime) {
        // 查询好友列表，添加时间筛选条件
        List<Friend> friends = friendMapper.findFriendsByUserIdAndTime(userId, offlineTime);

        if (friends == null || friends.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取好友ID列表
        List<Long> friendIds = friends.stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());

        // 查询会话信息，筛选类型为单聊（1）
        List<Session> sessions = sessionMapper.findSessionsByUserIdsAndType(userId, SESSION_TYPE_SINGLE, offlineTime);

        // 转换为NewSessionNotification DTO
        return friends.stream()
                .flatMap(friend -> sessions.stream()
                        .map(session -> new NewSessionNotification(String.valueOf(friend.getFriendId()),
                                String.valueOf(session.getId()),
                                session.getType(),
                                getUserName(friend.getFriendId()),
                                getUserAvatar(friend.getFriendId()))))
                .collect(Collectors.toList());
    }

    /**
     * 获取新群聊推送
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间
     * @return 新群聊推送列表
     */
    private List<NewGroupSessionNotification> getNewGroupPushes(Long userId, LocalDateTime offlineTime) {
        // 查询用户会话关系，筛选类型为群聊（2）并添加时间筛选条件
        List<UserSession> userSessions = userSessionMapper.findGroupSessionsByUserIdAndTime(userId, SESSION_TYPE_GROUP, offlineTime);

        if (userSessions == null || userSessions.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取会话ID列表
        List<Long> sessionIds = userSessions.stream()
                .map(UserSession::getSessionId)
                .collect(Collectors.toList());

        // 查询群聊会话信息
        List<Session> sessions = sessionMapper.findSessionsByIds(sessionIds);

        // 转换为NewGroupSessionNotification DTO
        return sessions.stream()
                .map(session -> new NewGroupSessionNotification(
                        null,
                        String.valueOf(session.getId()),
                        session.getType(),
                        session.getName(),
                        "http://localhost:8080/img/avatar/IM_GROUP.jpg"
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取好友申请推送
     *
     * @param userId      用户ID
     * @param offlineTime 用户离线时间
     * @return 好友申请推送列表
     */
    private List<FriendApplicationNotification> getFriendRequests(Long userId, LocalDateTime offlineTime) {
        // 查询好友申请表，筛选target_id为用户ID且时间在offlineTime之后
        List<ApplyFriend> applyFriends = applyFriendMapper.findApplyFriendsByTargetIdAndTime(userId, offlineTime);

        if (applyFriends == null || applyFriends.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为FriendRequest DTO
        return applyFriends.stream()
                .map(apply -> new FriendApplicationNotification(
                        getUserName(apply.getUserId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户头像
     *
     * @param userId 用户ID
     * @return 用户头像URL
     */
    private String getUserAvatar(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            return user.getAvatar();
        }
        return "";
    }


    /**
     * 获取用户名称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    private String getUserName(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            return user.getUserName();
        }
        return "";
    }
}