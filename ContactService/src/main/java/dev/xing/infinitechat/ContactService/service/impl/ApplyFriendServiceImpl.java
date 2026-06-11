package dev.xing.infinitechat.ContactService.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import dev.xing.infinitechat.common.Result;
import dev.xing.infinitechat.common.ResultGenerator;
import dev.xing.infinitechat.common.ServiceException;
import dev.xing.infinitechat.ContactService.enums.ConfigEnum;
import dev.xing.infinitechat.ContactService.enums.FriendApplicationStatus;
import dev.xing.infinitechat.ContactService.constants.FriendRequestConstants;
import dev.xing.infinitechat.ContactService.model.entity.User;
import dev.xing.infinitechat.ContactService.model.dto.ApplyFriendDTO;
import dev.xing.infinitechat.ContactService.mapper.ApplyFriendMapper;
import dev.xing.infinitechat.ContactService.model.entity.ApplyFriend;
import dev.xing.infinitechat.ContactService.model.dto.ModifyFriendApplicationResponse;
import dev.xing.infinitechat.ContactService.model.dto.PageResult;
import dev.xing.infinitechat.ContactService.model.dto.push.FriendApplicationNotification;
import dev.xing.infinitechat.ContactService.service.ApplyFriendService;
import dev.xing.infinitechat.ContactService.service.FriendService;
import dev.xing.infinitechat.ContactService.service.PushService;
import dev.xing.infinitechat.ContactService.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/**
 * 好友申请服务实现类
 */
@Slf4j
@Service
public class ApplyFriendServiceImpl extends ServiceImpl<ApplyFriendMapper, ApplyFriend> implements ApplyFriendService {

    private final ApplyFriendMapper applyFriendMapper;
    private final FriendService friendService;
    private final UserService userService;
    private final PushService pushService;
    private final StringRedisTemplate redisTemplate;
    private final Snowflake snowflake;

    @Autowired
    public ApplyFriendServiceImpl(ApplyFriendMapper applyFriendMapper,
                                  FriendService friendService,
                                  UserService userService,
                                  PushService pushService,
                                  StringRedisTemplate redisTemplate) {
        this.applyFriendMapper = applyFriendMapper;
        this.friendService = friendService;
        this.userService = userService;
        this.pushService = pushService;
        this.redisTemplate = redisTemplate;
        this.snowflake = IdUtil.getSnowflake(
                Integer.parseInt(ConfigEnum.WORKED_ID.getValue()),
                Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
    }

    /**
     * 发送好友申请
     *
     * @param userUuid        发送者用户UUID
     * @param receiveUserUuid 接收者用户UUID
     * @param msg             申请信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean addFriend(String userUuid, String receiveUserUuid, String msg) {
        Long senderId = convertUuidToId(userUuid, FriendRequestConstants.APPLICANT_NOT_FOUND);
        Long receiverId = convertUuidToId(receiveUserUuid, "接收者用户不存在");

        User applicant = getUserById(senderId, FriendRequestConstants.APPLICANT_NOT_FOUND);
        FriendApplicationNotification notification = createNotification(applicant);

        ApplyFriend existingApplyFriend = findExistingApplyFriend(senderId, receiverId);

        if (existingApplyFriend == null) {
            return handleNewFriendApplication(senderId, receiverId, msg, notification);
        } else if (existingApplyFriend.getStatus() == FriendApplicationStatus.ACCEPTED.getCode()) {
            throw new ServiceException(FriendRequestConstants.FRIEND_ALREADY_ADDED);
        } else {
            return handleExistingFriendApplication(existingApplyFriend, msg, notification);
        }
    }

    /**
     * 获取好友申请列表
     *
     * @param userUuid 用户UUID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param key      关键字
     * @return 申请列表结果
     */
    @Override
    public Result getApplyList(Long userUuid, int pageNum, int pageSize, String key) {
        try {
            Page<ApplyFriend> page = new Page<>(pageNum, pageSize);
            QueryWrapper<ApplyFriend> queryWrapper = buildApplyListQuery(userUuid, key);

            Page<ApplyFriend> applyFriendPage = applyFriendMapper.selectPage(page, queryWrapper.orderByDesc(FriendRequestConstants.CREATED_AT));
            List<ApplyFriendDTO> dtoList = mapApplyFriendsToDTO(applyFriendPage.getRecords(), userUuid);

            PageResult<ApplyFriendDTO> resultData = new PageResult<>();
            resultData.setList(dtoList);
            resultData.setTotal((int) applyFriendPage.getTotal());

            return ResultGenerator.genSuccessResult(resultData);
        } catch (Exception e) {
            log.error("获取申请添加好友列表失败: {}", e.getMessage(), e);
            throw new ServiceException("获取申请添加好友列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读好友申请数量
     *
     * @param userUuid 用户UUID
     * @return 未读数量
     */
    @Override
    public int getApplyCount(Long userUuid) {
        if (userUuid == null) {
            throw new ServiceException("用户ID不能为空");
        }
        try {
            return applyFriendMapper.countUnreadFriendRequests(userUuid);
        } catch (Exception e) {
            log.error("系统异常：获取用户{}的未读好友申请数量失败，原因：{}", userUuid, e.getMessage(), e);
            throw new ServiceException("获取未读好友申请数量失败", e);
        }
    }

    /**
     * 修改好友申请状态
     *
     * @param userUuid          当前用户UUID
     * @param status            新状态
     * @param receiveUserUuids  接收者UUID列表
     * @return 修改结果
     */
    @Override
    @Transactional
    public ModifyFriendApplicationResponse modifyFriendApplicationStatus(String userUuid, Integer status, List<String> receiveUserUuids) {
        FriendApplicationStatus newStatus = validateAndGetStatus(status);
        User currentUser = getUserById(convertUuidToId(userUuid, "用户不存在"));

        List<Long> receiveUserIds = getReceiveUserIds(receiveUserUuids);

        switch (newStatus) {
            case ACCEPTED:
                return handleAcceptStatus(currentUser, receiveUserIds);
            case READ:
                return handleReadStatus(currentUser, receiveUserIds);
            case REJECTED:
            case EXPIRED:
            default:
                throw new ServiceException("不允许修改为该状态值");
        }
    }

    /* ===================== 私有方法 ===================== */

    /**
     * 将用户UUID转换为用户ID，并进行校验
     *
     * @param userUuid   用户UUID
     * @param errorMsg   错误信息
     * @return 用户ID
     */
    private Long convertUuidToId(String userUuid, String errorMsg) {
        try {
            return Long.valueOf(userUuid);
        } catch (NumberFormatException e) {
            throw new ServiceException(errorMsg);
        }
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @param errorMsg 错误信息
     * @return 用户信息
     */
    private User getUserById(Long userId, String errorMsg) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new ServiceException(errorMsg);
        }
        return user;
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    private User getUserById(Long userId) {
        return getUserById(userId, "用户不存在");
    }

    /**
     * 创建好友申请通知
     *
     * @param applicant 申请者
     * @return 通知对象
     */
    private FriendApplicationNotification createNotification(User applicant) {
        FriendApplicationNotification notification = new FriendApplicationNotification();
        notification.setApplyUserName(applicant.getUserName());
        return notification;
    }

    /**
     * 查找现有的好友申请
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @return 现有的好友申请
     */
    private ApplyFriend findExistingApplyFriend(Long senderId, Long receiverId) {
        QueryWrapper<ApplyFriend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(FriendRequestConstants.USER_ID, senderId)
                .eq(FriendRequestConstants.TARGET_ID, receiverId);
        return this.getOne(queryWrapper);
    }

    /**
     * 处理新的好友申请
     *
     * @param senderId    发送者ID
     * @param receiverId  接收者ID
     * @param msg         申请信息
     * @param notification 通知对象
     * @return 是否成功
     */
    private boolean handleNewFriendApplication(Long senderId, Long receiverId, String msg, FriendApplicationNotification notification) {
        ApplyFriend newApplyFriend = createApplyFriend(senderId, receiverId, msg);
        pushNotification(receiverId, notification);
        boolean isSaved = this.save(newApplyFriend);
        if (isSaved) {
            setFriendRequestExpiration(newApplyFriend.getId());
        }
        return isSaved;
    }

    /**
     * 创建一个新的好友申请对象
     *
     * @param senderId    发送者ID
     * @param receiverId  接收者ID
     * @param msg         申请信息
     * @return ApplyFriend 对象
     */
    private ApplyFriend createApplyFriend(Long senderId, Long receiverId, String msg) {
        ApplyFriend applyFriend = new ApplyFriend();
        applyFriend.setId(snowflake.nextId());
        applyFriend.setUserId(senderId);
        applyFriend.setTargetId(receiverId);
        applyFriend.setMsg(msg);
        applyFriend.setStatus(FriendApplicationStatus.UNREAD.getCode());
        return applyFriend;
    }

    /**
     * 处理已有的好友申请
     *
     * @param existingApplyFriend 现有的好友申请
     * @param msg                 申请信息
     * @param notification        通知对象
     * @return 是否成功
     */
    private boolean handleExistingFriendApplication(ApplyFriend existingApplyFriend, String msg, FriendApplicationNotification notification) {
        existingApplyFriend.setStatus(FriendApplicationStatus.UNREAD.getCode());
        existingApplyFriend.setMsg(msg);
        existingApplyFriend.setUpdatedAt(LocalDateTime.now());
        pushNotification(existingApplyFriend.getTargetId(), notification);
        boolean isUpdated = this.updateById(existingApplyFriend);
        if (isUpdated) {
            setFriendRequestExpiration(existingApplyFriend.getId());
        }
        return isUpdated;
    }

    /**
     * 推送好友申请通知
     *
     * @param receiverId   接收者ID
     * @param notification 通知对象
     */
    private void pushNotification(Long receiverId, FriendApplicationNotification notification) {
        try {
            pushService.pushNewApply(receiverId, notification);
        } catch (Exception e) {
            log.warn(FriendRequestConstants.PUSH_FAILURE_LOG, e.getMessage());
        }
    }

    /**
     * 设置好友申请在Redis中的过期键
     *
     * @param applyFriendId 好友申请ID
     */
    private void setFriendRequestExpiration(Long applyFriendId) {
        String redisKey = FriendRequestConstants.FRIEND_REQUEST_KEY_PREFIX + applyFriendId;
        redisTemplate.opsForValue().set(redisKey, FriendRequestConstants.ACTIVE_STATUS,
                FriendRequestConstants.FRIEND_REQUEST_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 构建获取好友申请列表的查询条件
     *
     * @param userUuid 用户UUID
     * @param key      关键字
     * @return 查询条件
     */
    private QueryWrapper<ApplyFriend> buildApplyListQuery(Long userUuid, String key) {
        QueryWrapper<ApplyFriend> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(FriendRequestConstants.USER_ID, userUuid)
                        .or()
                        .eq(FriendRequestConstants.TARGET_ID, userUuid)
        );

        if (key != null && !key.trim().isEmpty()) {
            List<Long> matchedUserIds = findMatchedUserIds(key);
            if (!matchedUserIds.isEmpty()) {
                queryWrapper.and(wrapper1 -> wrapper1.in(FriendRequestConstants.USER_ID, matchedUserIds)
                        .or()
                        .in(FriendRequestConstants.TARGET_ID, matchedUserIds));
            } else {
                // 返回空结果
                queryWrapper.eq(FriendRequestConstants.USER_ID, -1L);
            }
        }

        return queryWrapper;
    }

    /**
     * 根据关键字查找匹配的用户ID
     *
     * @param key 关键字
     * @return 匹配的用户ID列表
     */
    private List<Long> findMatchedUserIds(String key) {
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.like(FriendRequestConstants.USER_NAME, key)
                .or()
                .like(FriendRequestConstants.USER_ID_STR, key)
                .or()
                .like(FriendRequestConstants.PHONE, key);
        List<User> matchedUsers = userService.list(userQuery);
        return matchedUsers.stream().map(User::getUserId).collect(Collectors.toList());
    }

    /**
     * 将好友申请记录映射为DTO对象列表
     *
     * @param applyFriends 好友申请记录
     * @param userUuid     当前用户UUID
     * @return DTO对象列表
     */
    private List<ApplyFriendDTO> mapApplyFriendsToDTO(List<ApplyFriend> applyFriends, Long userUuid) {
        List<ApplyFriendDTO> dtoList = new ArrayList<>();
        for (ApplyFriend applyFriend : applyFriends) {
            ApplyFriendDTO dto = new ApplyFriendDTO();
            dto.setMsg(applyFriend.getMsg());
            dto.setStatus(applyFriend.getStatus());
            dto.setTime(applyFriend.getCreatedAt());

            if (applyFriend.getUserId().equals(userUuid)) {
                User targetUser = getUserById(applyFriend.getTargetId(), "接收者用户不存在");
                dto.setUserUuid(String.valueOf(targetUser.getUserId()));
                dto.setNickname(targetUser.getUserName());
                dto.setAvatar(targetUser.getAvatar());
                dto.setIsReceiver(FriendRequestConstants.IS_RECEIVER_NO);
            } else {
                User senderUser = getUserById(applyFriend.getUserId(), "发送者用户不存在");
                dto.setUserUuid(String.valueOf(senderUser.getUserId()));
                dto.setNickname(senderUser.getUserName());
                dto.setAvatar(senderUser.getAvatar());
                dto.setIsReceiver(FriendRequestConstants.IS_RECEIVER_YES);
            }

            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * 验证并获取好友申请状态枚举
     *
     * @param status 状态码
     * @return 状态枚举
     */
    private FriendApplicationStatus validateAndGetStatus(Integer status) {
        try {
            return FriendApplicationStatus.fromCode(status);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("不允许修改为该状态值");
        }
    }

    /**
     * 获取接收者用户ID列表
     *
     * @param receiveUserUuids 接收者UUID列表
     * @return 接收者ID列表
     */
    private List<Long> getReceiveUserIds(List<String> receiveUserUuids) {
        return receiveUserUuids.stream()
                .map(uuid -> convertUuidToId(uuid, "接收者用户不存在"))
                .collect(Collectors.toList());
    }

    /**
     * 处理接受状态的修改
     *
     * @param currentUser   当前用户
     * @param receiveUserIds 接收者ID列表
     * @return 修改结果
     */
    private ModifyFriendApplicationResponse handleAcceptStatus(User currentUser, List<Long> receiveUserIds) {
        if (receiveUserIds.size() != 1) {
            throw new ServiceException("通过状态只能包含一个接收者");
        }
        Long acceptTargetId = receiveUserIds.get(0);

        int updated = applyFriendMapper.updateStatusByUserAndTarget(
                FriendApplicationStatus.ACCEPTED.getCode(),
                acceptTargetId,
                currentUser.getUserId());
        if (updated == 0) {
            throw new ServiceException("没有待处理的好友申请");
        }

        return friendService.addFriend(currentUser, acceptTargetId);
    }

    /**
     * 处理已读状态的修改
     *
     * @param currentUser   当前用户
     * @param receiveUserIds 接收者ID列表
     */
    private ModifyFriendApplicationResponse handleReadStatus(User currentUser, List<Long> receiveUserIds) {
        int updated = applyFriendMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ApplyFriend>()
                        .set(FriendRequestConstants.STATUS, FriendApplicationStatus.READ.getCode())
                        .eq(FriendRequestConstants.TARGET_ID, currentUser.getUserId())
                        .in(FriendRequestConstants.USER_ID, receiveUserIds)
                        .eq(FriendRequestConstants.STATUS, FriendApplicationStatus.UNREAD.getCode()));
        if (updated == 0) {
            throw new ServiceException("没有未读的好友申请");
        }
        return null;
    }
}