package dev.xing.infinitechat.ContactService.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.constants.FriendServiceConstants;
import dev.xing.infinitechat.ContactService.enums.ConfigEnum;
import dev.xing.infinitechat.ContactService.enums.SessionType;
import dev.xing.infinitechat.ContactService.mapper.ApplyFriendMapper;
import dev.xing.infinitechat.ContactService.mapper.SessionMapper;
import dev.xing.infinitechat.ContactService.mapper.UserSessionMapper;
import dev.xing.infinitechat.ContactService.model.dto.FriendDTO;
import dev.xing.infinitechat.ContactService.mapper.FriendMapper;
import dev.xing.infinitechat.ContactService.model.dto.ModifyFriendApplicationResponse;
import dev.xing.infinitechat.ContactService.model.dto.push.NewSessionNotification;
import dev.xing.infinitechat.ContactService.model.entity.*;
import dev.xing.infinitechat.ContactService.model.vo.FriendDetailVO;
import dev.xing.infinitechat.ContactService.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PushService pushService;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private UserSessionMapper userSessionMapper;

    @Autowired
    private ApplyFriendMapper applyFriendMapper;

    @Autowired
    private FriendMapper friendMapper;

    private final Snowflake snowflake = IdUtil.getSnowflake(
            Integer.parseInt(ConfigEnum.WORKED_ID.getValue()),
            Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue())
    );

    /**
     * 获取用户的好友列表，支持分页和关键字搜索
     *
     * @param userUuid 用户唯一标识
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param key      搜索关键字
     * @return 分页的好友DTO列表
     */
    @Override
    public IPage<FriendDTO> getFriends(Long userUuid, int pageNum, int pageSize, String key) {
        validateUserId(userUuid);
        Page<FriendDTO> page = new Page<>(pageNum, pageSize);
        String searchKey = StringUtils.hasText(key) ? key : FriendServiceConstants.EMPTY_STRING;

        try {
            return friendMapper.findFriendsByUserId(page, userUuid, searchKey);
        } catch (Exception e) {
            log.error(FriendServiceConstants.GET_FRIENDS_FAILED, e);
            throw new ServiceException(FriendServiceConstants.GET_FRIENDS_FAILED);
        }
    }

    /**
     * 校验用户ID的有效性
     *
     * @param userId 用户ID
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId < 0) {
            throw new ServiceException(FriendServiceConstants.INVALID_USER_ID);
        }
    }

    /**
     * 删除好友的业务逻辑实现
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 删除是否成功
     */
    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        validateUserId(userId);
        validateUserId(friendId);

        try {
            deleteApplyFriendRecords(userId, friendId);
            deleteFriendRecords(userId, friendId);
            deleteSessionRecords(userId, friendId);
            return true;
        } catch (Exception e) {
            log.error(FriendServiceConstants.DELETE_FRIEND_FAILED, e);
            throw new ServiceException(FriendServiceConstants.DELETE_FRIEND_FAILED);
        }
    }

    /**
     * 删除 apply_friend 表中的相关记录
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     */
    private void deleteApplyFriendRecords(Long userId, Long friendId) {
        QueryWrapper<ApplyFriend> applyFriendWrapper = new QueryWrapper<>();
        applyFriendWrapper
                .nested(wrapper -> wrapper.eq("user_id", userId).eq("target_id", friendId))
                .or()
                .nested(wrapper -> wrapper.eq("user_id", friendId).eq("target_id", userId));
        applyFriendMapper.delete(applyFriendWrapper);
    }

    /**
     * 删除 friend 表中的相关记录
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     */
    private void deleteFriendRecords(Long userId, Long friendId) {
        QueryWrapper<Friend> friendWrapper = new QueryWrapper<>();
        friendWrapper
                .nested(wrapper -> wrapper.eq("user_id", userId).eq("friend_id", friendId))
                .or()
                .nested(wrapper -> wrapper.eq("user_id", friendId).eq("friend_id", userId));
        baseMapper.delete(friendWrapper);
    }

    /**
     * 删除 session 和 user_session 表中的相关记录
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     */
    private void deleteSessionRecords(Long userId, Long friendId) {
        List<Long> sessionIds = sessionMapper.selectSessionIdsBetweenUsers(userId, friendId);
        if (sessionIds != null && !sessionIds.isEmpty()) {
            deleteUserSessionRecords(sessionIds);
            sessionMapper.deleteBatchIds(sessionIds);
        }
    }

    /**
     * 删除 user_session 表中的记录
     *
     * @param sessionIds 会话ID列表
     */
    private void deleteUserSessionRecords(List<Long> sessionIds) {
        QueryWrapper<UserSession> userSessionWrapper = new QueryWrapper<>();
        userSessionWrapper.in("session_id", sessionIds);
        userSessionMapper.delete(userSessionWrapper);
    }

    /**
     * 拉黑好友的业务逻辑实现
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 更新是否成功
     */
    @Override
    @Transactional
    public boolean blockFriend(Long userId, Long friendId) {
        validateUserId(userId);
        validateUserId(friendId);

        Friend friend = getFriendRelation(userId, friendId);
        validateFriendRelation(friend, userId);

        friend.setStatus(FriendServiceConstants.FRIEND_STATUS_BLOCKED);
        return this.updateById(friend);
    }

    /**
     * 获取好友关系
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return Friend 实体
     */
    private Friend getFriendRelation(Long userId, Long friendId) {
        return this.lambdaQuery()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .one();
    }

    /**
     * 验证好友关系是否存在以及权限
     *
     * @param friend Friend 实体
     * @param userId 当前用户ID
     */
    private void validateFriendRelation(Friend friend, Long userId) {
        if (ObjectUtils.isEmpty(friend)) {
            throw new ServiceException(FriendServiceConstants.FRIEND_NOT_EXIST);
        }
        if (!friend.getUserId().equals(userId)) {
            throw new ServiceException(FriendServiceConstants.UNAUTHORIZED_OPERATION);
        }
    }

    /**
     * 获取好友的详细信息
     *
     * @param userUuid   当前用户UUID
     * @param friendUuid 好友UUID
     * @return FriendDetailVO 对象
     */
    @Override
    public FriendDetailVO getFriendDetails(String userUuid, String friendUuid) {
        Long userId = parseUserId(userUuid);
        Long friendId = parseUserId(friendUuid);

        User friendUser = userService.getById(friendId);
        validateFriendUser(friendUser);

        FriendDetailVO friendDetailVO = buildFriendDetailVO(userId, friendUser);
        populateSessionId(userId, friendId, friendDetailVO);
        populateFriendStatus(userId, friendId, friendDetailVO);

        return friendDetailVO;
    }

    /**
     * 解析并验证用户ID
     *
     * @param userUuid 用户UUID字符串
     * @return 解析后的用户ID
     */
    private Long parseUserId(String userUuid) {
        try {
            return Long.valueOf(userUuid);
        } catch (NumberFormatException e) {
            throw new ServiceException(FriendServiceConstants.INVALID_USER_ID);
        }
    }

    /**
     * 验证好友用户是否存在及其状态
     *
     * @param friendUser 好友的User实体
     */
    private void validateFriendUser(User friendUser) {
        if (friendUser == null) {
            throw new ServiceException(FriendServiceConstants.USER_NOT_EXIST);
        }
        switch (friendUser.getStatus()) {
            case FriendServiceConstants.USER_STATUS_BANNED:
                throw new ServiceException(FriendServiceConstants.USER_BANNED);
            case FriendServiceConstants.USER_STATUS_DELETED:
                throw new ServiceException(FriendServiceConstants.USER_DELETED);
            default:
                break;
        }
    }

    /**
     * 构建好友详细信息VO
     *
     * @param userId     当前用户ID
     * @param friendUser 好友的User实体
     * @return FriendDetailVO 对象
     */
    private FriendDetailVO buildFriendDetailVO(Long userId, User friendUser) {
        FriendDetailVO vo = new FriendDetailVO();
        vo.setUserUuid(String.valueOf(friendUser.getUserId()));
        vo.setNickname(friendUser.getUserName());
        vo.setAvatar(friendUser.getAvatar());
        vo.setEmail(friendUser.getEmail());
        vo.setPhone(friendUser.getPhone());
        vo.setSignature(friendUser.getSignature());
        vo.setGender(friendUser.getGender());
        return vo;
    }

    /**
     * 填充会话ID到FriendDetailVO
     *
     * @param userId         当前用户ID
     * @param friendId       好友ID
     * @param friendDetailVO FriendDetailVO 对象
     */
    private void populateSessionId(Long userId, Long friendId, FriendDetailVO friendDetailVO) {
        List<Long> commonSessionIds = userSessionMapper.findCommonSingleChatSessionIds(userId, friendId);
        if (commonSessionIds == null || commonSessionIds.isEmpty()) {
            friendDetailVO.setSessionId(null);
        } else {
            friendDetailVO.setSessionId(String.valueOf(commonSessionIds.get(0)));
        }
    }

    /**
     * 填充好友状态到FriendDetailVO
     *
     * @param userId         当前用户ID
     * @param friendId       好友ID
     * @param friendDetailVO FriendDetailVO 对象
     */
    private void populateFriendStatus(Long userId, Long friendId, FriendDetailVO friendDetailVO) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("friend_id", friendId)
                .eq("user_id", userId);
        Friend friend = this.getOne(wrapper);
        if (friend != null) {
            friendDetailVO.setStatus(friend.getStatus());
        } else {
            friendDetailVO.setStatus(FriendServiceConstants.FRIEND_STATUS_NON_FRIEND);
        }
    }

    /**
     * 根据手机号获取用户详情
     *
     * @param userId 当前用户ID
     * @param key    手机号
     * @return FriendDetailVO 对象
     */
    @Override
    public FriendDetailVO getUserDetails(String userId, String key) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("phone", key);
        User user = userService.getOne(userQueryWrapper);
        if (user == null) {
            throw new ServiceException(FriendServiceConstants.USER_NOT_EXIST);
        }
        return getFriendDetails(userId, String.valueOf(user.getUserId()));
    }

    /**
     * 添加好友的业务逻辑实现
     *
     * @param recipient 接收好友请求的用户
     * @param friendId  申请添加的好友ID
     * @return ModifyFriendApplicationResponse 响应对象
     */
    @Override
    @Transactional
    public ModifyFriendApplicationResponse addFriend(User recipient, Long friendId) {
        validateUserId(friendId);
        validateRecipient(recipient);

        User applicant = userService.getById(friendId);
        validateApplicant(applicant);

        checkIfAlreadyFriends(recipient.getUserId(), friendId);
        createFriendRelations(recipient.getUserId(), friendId);
        Long sessionId = createSession(recipient.getUserId(), friendId);
        createUserSessions(recipient.getUserId(), friendId, sessionId);
        sendPushNotification(recipient, friendId, sessionId);

        return buildModifyFriendApplicationResponse(applicant, sessionId);
    }

    /**
     * 验证接收者用户是否存在
     *
     * @param recipient 接收者User实体
     */
    private void validateRecipient(User recipient) {
        if (recipient == null) {
            throw new ServiceException(FriendServiceConstants.USER_NOT_EXIST);
        }
    }

    /**
     * 验证申请者用户是否存在
     *
     * @param applicant 申请者User实体
     */
    private void validateApplicant(User applicant) {
        if (applicant == null) {
            throw new ServiceException("好友申请者发送者不存在");
        }
    }

    /**
     * 检查是否已是好友关系
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     */
    private void checkIfAlreadyFriends(Long userId, Long friendId) {
        boolean exists = this.lambdaQuery()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .exists();
        if (exists) {
            throw new ServiceException(FriendServiceConstants.ALREADY_FRIENDS);
        }
    }

    /**
     * 创建双向好友关系
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     */
    private void createFriendRelations(Long userId, Long friendId) {
        Friend friend1 = new Friend();
        friend1.setId(snowflake.nextId());
        friend1.setUserId(userId);
        friend1.setFriendId(friendId);
        friend1.setStatus(FriendServiceConstants.FRIEND_STATUS_ACTIVE);

        Friend friend2 = new Friend();
        friend2.setId(snowflake.nextId());
        friend2.setUserId(friendId);
        friend2.setFriendId(userId);
        friend2.setStatus(FriendServiceConstants.FRIEND_STATUS_ACTIVE);

        boolean save1 = this.save(friend1);
        boolean save2 = this.save(friend2);

        if (!save1 || !save2) {
            throw new ServiceException(FriendServiceConstants.ADD_FRIEND_FAILED);
        }
    }

    /**
     * 创建会话
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @return 会话ID
     */
    private Long createSession(Long userId, Long friendId) {
        Long sessionId = snowflake.nextId();
        Session session = new Session();
        session.setId(sessionId);
        session.setName(FriendServiceConstants.EMPTY_STRING);
        session.setType(SessionType.SINGLE.getValue());
        session.setStatus(FriendServiceConstants.FRIEND_STATUS_ACTIVE);

        boolean sessionSaved = sessionService.save(session);
        if (!sessionSaved) {
            throw new ServiceException(FriendServiceConstants.CREATE_SESSION_FAILED);
        }
        return sessionId;
    }

    /**
     * 创建用户会话关系
     *
     * @param userId    当前用户ID
     * @param friendId  好友ID
     * @param sessionId 会话ID
     */
    private void createUserSessions(Long userId, Long friendId, Long sessionId) {
        UserSession userSession1 = new UserSession();
        userSession1.setId(snowflake.nextId());
        userSession1.setUserId(userId);
        userSession1.setSessionId(sessionId);
        userSession1.setRole(FriendServiceConstants.USER_ROLE_NORMAL);
        userSession1.setStatus(FriendServiceConstants.FRIEND_STATUS_ACTIVE);

        UserSession userSession2 = new UserSession();
        userSession2.setId(snowflake.nextId());
        userSession2.setUserId(friendId);
        userSession2.setSessionId(sessionId);
        userSession2.setRole(FriendServiceConstants.USER_ROLE_NORMAL);
        userSession2.setStatus(FriendServiceConstants.FRIEND_STATUS_ACTIVE);

        boolean userSessionSaved1 = userSessionService.save(userSession1);
        boolean userSessionSaved2 = userSessionService.save(userSession2);
        if (!userSessionSaved1 || !userSessionSaved2) {
            throw new ServiceException(FriendServiceConstants.CREATE_USER_SESSION_FAILED);
        }
    }

    /**
     * 发送推送通知
     *
     * @param recipient 接收者User实体
     * @param friendId  好友ID
     * @param sessionId 会话ID
     */
    private void sendPushNotification(User recipient, Long friendId, Long sessionId) {
        NewSessionNotification notification = new NewSessionNotification();
        notification.setUserId(String.valueOf(recipient.getUserId()));
        notification.setSessionId(String.valueOf(sessionId));
        notification.setSessionType(SessionType.SINGLE.getValue());
        notification.setSessionName(recipient.getUserName());
        notification.setAvatar(recipient.getAvatar());

        try {
            pushService.pushNewSession(friendId, notification);
        } catch (Exception e) {
            log.info(FriendServiceConstants.PUSH_NOTIFICATION_FAILED + ": " + e.getMessage());
        }
    }

    /**
     * 构建 ModifyFriendApplicationResponse 响应对象
     *
     * @param applicant 申请者User实体
     * @param sessionId 会话ID
     * @return ModifyFriendApplicationResponse 对象
     */
    private ModifyFriendApplicationResponse buildModifyFriendApplicationResponse(User applicant, Long sessionId) {
        ModifyFriendApplicationResponse response = new ModifyFriendApplicationResponse();
        response.setUserId(String.valueOf(applicant.getUserId()));
        response.setSessionId(String.valueOf(sessionId));
        response.setSessionType(SessionType.SINGLE.getValue());
        response.setSessionName(applicant.getUserName());
        response.setAvatar(applicant.getAvatar());
        return response;
    }
}