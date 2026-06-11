package dev.xing.infinitechat.messagingservice.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LockService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 自适应等待时间获取分布式redis锁
     *
     * @param lockKey    锁的键
     * @param maxRetries 最大重试次数
     * @param userId 用户Id
     * @return 是否成功获取锁
     */
    public boolean tryLockWithBackoff(String lockKey, int maxRetries,Long userId) {
        RLock lock = redissonClient.getLock(lockKey);
        long initialWaitTime = 250; // 初始等待时间 0.25 秒
        long maxWaitTime = 2000; // 最大等待时间 2 秒
        boolean acquired = false;

        for (int i = 0; i < maxRetries; i++) {
            try {
                // 尝试在 5 秒内获取锁（锁持有时间为5秒）
                acquired = lock.tryLock(5, 5, TimeUnit.SECONDS);

                if (acquired) {
                    log.info(userId + " 成功获取锁！");
                    return true;
                } else {
                    // 没有获取到锁，等待一段时间后继续尝试
                    long waitTime = Math.min(initialWaitTime * (i + 1), maxWaitTime);
                    log.info("等待 " + waitTime + " 毫秒后重试...");
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("线程中断");
                return false;
            } catch (Exception e) {
                log.info(userId + " 获取锁失败：" + e.getMessage());
                return false;
            }
        }

        // 如果超过最大重试次数仍然没有获取到锁
        log.info("系统繁忙，无法获取锁");
        return false;
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的键
     */
    public void releaseLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("锁已释放");
        }
    }











}
