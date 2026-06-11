package dev.xing.infinitechat.ContactService.listener;

import java.util.Objects;

import dev.xing.infinitechat.ContactService.constants.FriendRequestConstants;
import dev.xing.infinitechat.ContactService.model.entity.ApplyFriend;
import dev.xing.infinitechat.ContactService.service.ApplyFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监听 Redis 中好友申请过期事件的监听器
 */
@Component
public class FriendRequestExpiryListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(FriendRequestExpiryListener.class);

    @Autowired
    private ApplyFriendService applyFriendService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Get the expired key
        String expiredKey = message.toString();
        log.info("得到过期的键: " + expiredKey);
        handleExpiredKey(expiredKey);
    }

    /**
     * 处理过期的键
     *
     * @param expiredKey 过期的键名
     */
    public void handleExpiredKey(String expiredKey) {
        log.info("Received expired key: " + expiredKey);
        // 检查是否是好友申请的键
        if (expiredKey != null && expiredKey.startsWith(FriendRequestConstants.FRIEND_REQUEST_KEY_PREFIX)) {
            try {
                // 从键中提取好友申请ID
                Long applyFriendId = extractApplyFriendId(expiredKey);
                if (applyFriendId != null) {
                    processExpiredFriendRequest(applyFriendId);
                }
            } catch (Exception e) {
                log.error("处理好友申请过期事件失败，键名：{}，错误：{}", expiredKey, e.getMessage());
            }
        }
    }

    /**
     * 从 Redis 键名中提取好友申请ID
     *
     * @param key Redis 键名
     * @return 好友申请ID，若无法提取则返回 null
     */
    private Long extractApplyFriendId(String key) {
        try {
            String idStr = key.substring(FriendRequestConstants.FRIEND_REQUEST_KEY_PREFIX.length());
            return Long.valueOf(idStr);
        } catch (Exception e) {
            log.error("提取好友申请ID失败，键名：{}，错误：{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 处理过期的好友申请
     *
     * @param applyFriendId 好友申请ID
     */
    private void processExpiredFriendRequest(Long applyFriendId) {
        // 根据ID查询好友申请
        ApplyFriend applyFriend = applyFriendService.getById(applyFriendId);
        if (applyFriend == null) {
            log.info("好友申请不存在，ID：{}", applyFriendId);
            return;
        }

        // 检查好友申请状态
        if (!Objects.equals(applyFriend.getStatus(), FriendRequestConstants.STATUS_ACCEPTED)) {
            // 更新状态为已过期
            applyFriend.setStatus(FriendRequestConstants.STATUS_EXPIRED);
            boolean isUpdated = applyFriendService.updateById(applyFriend);
            if (isUpdated) {
                log.info("好友申请已过期，ID：{}", applyFriendId);
            } else {
                log.error("更新好友申请状态为过期失败，ID：{}", applyFriendId);
            }
        } else {
            log.info("好友申请已被接受，无需过期处理，ID：{}", applyFriendId);
        }
    }


}
