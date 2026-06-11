package dev.xing.infinitechat.authenticationservice.service.impl;

import dev.xing.infinitechat.authenticationservice.service.DistributeLockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName DistributeLockServiceImpl
 * @Description 分布式锁服务实现类
 * @Date 2025/1/5 16:39
 */
@Service
public class DistributeLockServiceImpl implements DistributeLockService {
    @Autowired
    RedissonClient redissonClient;

//    // key 分布式锁 key
//    private String key;
//
//    // tryLockTime 尝试获得锁的时间（单位：毫秒）
//    private Long tryLockTime = 1000*3L;
//
//    // holdLockTime 持有锁时长（单位：毫秒）
//    private Long holdLockTime = 1000*5L;

    /***
     * @MethodName lock
     * @Description 加锁
     * @param: key
     * @param: tryLockTime
     * @param: holdLockTime
     * @return: boolean
     * @Date 2025/1/5 17:39
     */
    @Override
    public boolean lock(String key, Long tryLockTime, Long holdLockTime) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);

        return lock.tryLock(tryLockTime, holdLockTime,  TimeUnit.MILLISECONDS);
    }

    /***
     * @MethodName unLock
     * @Description 解锁
     * @param: key
     * @Date 2025/1/5 17:39
     */
    @Override
    public void unLock(String key){
        RLock lock = redissonClient.getLock(key);

        lock.unlock();
    }
}
