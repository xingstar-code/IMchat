package dev.xing.infinitechat.ContactService.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.enums.ConfigEnum;
import dev.xing.infinitechat.ContactService.enums.SessionType;
import dev.xing.infinitechat.ContactService.enums.UserRole;
import dev.xing.infinitechat.ContactService.enums.UserStatus;
import dev.xing.infinitechat.ContactService.mapper.FriendMapper;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.mapper.UserMapper;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.entity.Session;
import dev.xing.infinitechat.ContactService.model.entity.User;
import dev.xing.infinitechat.ContactService.model.entity.UserSession;
import dev.xing.infinitechat.ContactService.model.dto.CreateGroupRequest;
import dev.xing.infinitechat.ContactService.model.dto.CreateGroupResponse;
import dev.xing.infinitechat.ContactService.model.dto.push.NewGroupSessionNotification;
import dev.xing.infinitechat.ContactService.service.PushService;
import dev.xing.infinitechat.ContactService.service.SessionService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 */
@Slf4j
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private PushService pushService;

    /**
     * 处理群聊创建逻辑
     *
     * @param request 群聊创建请求参数
     * @return 创建结果
     * @throws ServiceException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateGroupResponse createGroup(CreateGroupRequest request) throws ServiceException {
        Long creatorId = request.getCreatorId();
        List<Long> memberIds = request.getMemberIds();
        List<String> failedMemberIds = new ArrayList<>();

        // 参数校验
        validateCreateGroupParameters(creatorId, memberIds);

        // 确认创建者用户存在且状态正常
        User creator = getActiveUserById(creatorId);

        // 验证好友关系并获取有效成员ID
        List<Long> validMemberIds = validateAndFilterMembers(creatorId, memberIds, failedMemberIds);

        if (validMemberIds.isEmpty()) {
            throw new ServiceException("没有有效的好友可加入群聊");
        }

        // 生成sessionId
        Long sessionId = generateId();

        // 生成群名称
        String groupName = generateGroupName(creatorId, validMemberIds);

        // 插入session表
        Session session = createSession(sessionId, groupName);
        sessionMapper.insert(session);

        // 插入user_session表 - 创建者
        insertUserSession(sessionId, creatorId);

        // 构建推送新群会话消息
        NewGroupSessionNotification notification = buildNewGroupSessionNotification(creatorId, sessionId, groupName);

        // 插入user_session表 - 其他成员并推送通知
        insertMembersAndPushNotifications(validMemberIds, sessionId, notification, failedMemberIds);

        // 响应结果
        CreateGroupResponse response = new CreateGroupResponse();
        BeanUtils.copyProperties(notification, response);
        response.setFailedMemberIds(failedMemberIds);
        return response;
    }

    /**
     * 校验创建群聊请求参数的合法性
     *
     * @param creatorId 创建者用户ID
     * @param memberIds 成员用户ID列表
     * @throws ServiceException 参数校验失败时抛出异常
     */
    private void validateCreateGroupParameters(Long creatorId, List<Long> memberIds) throws ServiceException {
        if (creatorId == null) {
            throw new ServiceException("创建者ID不能为空");
        }
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ServiceException("成员ID列表不能为空");
        }
    }

    /**
     * 获取活跃用户信息
     *
     * @param userId 用户ID
     * @return 活跃用户对象
     * @throws ServiceException 用户不存在或状态不正常
     */
    private User getActiveUserById(Long userId) throws ServiceException {
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != UserStatus.NORMAL.getValue()) {
            throw new ServiceException("用户不存在或状态异常");
        }
        return user;
    }

    /**
     * 验证并过滤成员ID，返回有效的成员ID列表
     *
     * @param creatorId       创建者用户ID
     * @param memberIds       成员用户ID列表
     * @param failedMemberIds 邀请失败的成员ID列表
     * @return 有效的成员ID列表
     */
    private List<Long> validateAndFilterMembers(Long creatorId, List<Long> memberIds, List<String> failedMemberIds) {
        // 获取创建者所有好友ID
        List<Long> friendIds = friendMapper.getFriendIds(creatorId);
        Set<Long> friendIdSet = new HashSet<>(friendIds);
        List<Long> validMemberIds = new ArrayList<>();

        for (Long memberId : memberIds) {
            if (friendIdSet.contains(memberId)) {
                validMemberIds.add(memberId);
            } else {
                failedMemberIds.add(String.valueOf(memberId));
                log.info("成员ID {} 不是创建者的好友，无法加入群聊", memberId);
            }
        }

        return validMemberIds;
    }

    /**
     * 生成唯一的sessionId
     *
     * @return sessionId
     */
    private Long generateId() {
        Snowflake snowflake = IdUtil.getSnowflake(
                Integer.parseInt(ConfigEnum.WORKED_ID.getValue()),
                Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue())
        );
        return snowflake.nextId();
    }

    /**
     * 生成群名称，最多16个字符
     *
     * @param creatorId 创建者用户ID
     * @param memberIds 成员用户ID列表
     * @return 群名称
     */
    private String generateGroupName(Long creatorId, List<Long> memberIds) {
        StringBuilder groupNameBuilder = new StringBuilder();
        List<Long> allMemberIds = new ArrayList<>(memberIds);
        allMemberIds.add(0, creatorId); // 确保群主 ID 在首位

        // 查询所有用户信息
        List<User> users = userMapper.selectBatchIds(allMemberIds);

        // 构建 ID -> User 映射，确保顺序可控
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 按照 allMemberIds 的顺序拼接用户名
        for (Long memberId : allMemberIds) {
            User user = userMap.get(memberId);
            if (user != null) {
                if (groupNameBuilder.length() > 0) {
                    groupNameBuilder.append("、");
                }
                groupNameBuilder.append(user.getUserName());
                if (groupNameBuilder.length() >= 16) {
                    groupNameBuilder.setLength(16); // 截取前 16 个字符
                    break;
                }
            }
        }
        return groupNameBuilder.toString();
    }

    /**
     * 创建会话对象
     *
     * @param sessionId 会话ID
     * @param groupName 群名称
     * @return 会话对象
     */
    private Session createSession(Long sessionId, String groupName) {
        Session session = new Session();
        session.setId(sessionId);
        session.setName(groupName);
        session.setType(SessionType.GROUP.getValue());
        session.setStatus(UserStatus.NORMAL.getValue());
        return session;
    }

    /**
     * 插入用户会话关系
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    private void insertUserSession(Long sessionId, Long userId) {
        UserSession userSession = new UserSession();
        userSession.setId(generateId());
        userSession.setUserId(userId);
        userSession.setSessionId(sessionId);
        userSession.setRole(UserRole.GROUP_OWNER.getValue());
        userSession.setStatus(UserStatus.NORMAL.getValue());
        userSessionMapper.insert(userSession);
    }

    /**
     * 获取用户映射
     *
     * @param userIds 用户ID列表
     * @return 用户ID到用户对象的映射
     */
    private Map<Long, User> getUserMap(List<Long> userIds) {
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));
    }

    /**
     * 构建新群会话的通知消息
     *
     * @param creatorId 创建者ID
     * @param sessionId 会话ID
     * @param groupName 群名称
     * @return 新群会话通知对象
     */
    private NewGroupSessionNotification buildNewGroupSessionNotification(Long creatorId, Long sessionId, String groupName) {
        NewGroupSessionNotification notification = new NewGroupSessionNotification();
        notification.setCreatorId(String.valueOf(creatorId));
        notification.setSessionId(String.valueOf(sessionId));
        notification.setSessionName(groupName);
        notification.setSessionType(SessionType.GROUP.getValue());
        notification.setAvatar(ConfigEnum.GROUP_AVATAR_URL.getValue());
        return notification;
    }

    /**
     * 插入成员并推送通知
     *
     * @param memberIds       成员ID列表
     * @param sessionId       会话ID
     * @param notification    通知对象
     * @param failedMemberIds 邀请失败的成员ID列表
     */
    private void insertMembersAndPushNotifications(List<Long> memberIds, Long sessionId, NewGroupSessionNotification notification, List<String> failedMemberIds) {
        for (Long memberId : memberIds) {
            // 插入用户会话关系
            UserSession userSession = new UserSession();
            userSession.setId(generateId());
            userSession.setUserId(memberId);
            userSession.setSessionId(sessionId);
            userSession.setRole(UserRole.GROUP_MEMBER.getValue());
            userSession.setStatus(UserStatus.NORMAL.getValue());
            userSessionMapper.insert(userSession);

            // 推送通知
            try {
                pushService.pushGroupNewSession(memberId, notification);
            } catch (Exception e) {
                failedMemberIds.add(String.valueOf(memberId));
                log.error("推送群聊会话失败，成员ID {}，错误信息：{}", memberId, e.getMessage());
            }
        }
    }
}