package dev.xing.infinitechat.authenticationservice.service;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * @InterfaceName DistributeLoackService
 * @Description 分布式锁服务
 * @Date 2025/1/5 16:31
 */
public interface DistributeLockService {

    /***
     * @MethodName lock
     * @Description 加锁
     * @param: key
     * @param: tryLockTime
     * @param: holdLockTime
     * @return: boolean
     * @Date 2025/1/5 17:38
     */
    public boolean lock(String key, Long tryLockTime, Long holdLockTime) throws InterruptedException;

    /***
     * @MethodName unLock
     * @Description 解锁
     * @param: key
     * @Date 2025/1/5 17:38
     */
    public void unLock(String key);
}
