package dev.xing.infinitechat.authenticationservice.utils;

import lombok.Data;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DistributeLockUtil
 * @Description 分布式锁工具类
 * @Date 2025/1/5 16:36
 */
@Data
public class DistributeLockUtil {

    // RedissonClient redisson 客户端
    private final RedissonClient redissonClient;

    // key 分布式锁 key
    private String key;

    // tryLockTime 尝试获得锁的时间（单位：毫秒）
    private Long tryLockTime = 1000*3L;

    // holdLockTime 持有锁时长（单位：毫秒）
    private Long holdLockTime = 1000*5L;

    /***
     * @MethodName DistributeLockUtil
     * @Description 构造器，key 为必传参数
     * @param: key
     * @param: redissonClient
     * @Date 2025/1/5 21:19
     */
    public DistributeLockUtil(String key, RedissonClient redissonClient){
        this.key = key;
        this.redissonClient = redissonClient;
    }

    /***
     * @MethodName DistributeLockUtil
     * @Description 构造函数，key 和 尝试获取锁的时间为必传参数
     * @param: key
     * @param: tryLockTime
     * @param: redissonClient
     * @Date 2025/1/5 21:19
     */
    public DistributeLockUtil(String key, Long tryLockTime, RedissonClient redissonClient){
        this.key = key;
        this.tryLockTime = tryLockTime;
        this.redissonClient = redissonClient;
    }

    /***
     * @MethodName DistributeLockUtil
     * @Description 全参数构造器
     * @param: key
     * @param: tryLockTime
     * @param: holdLockTime
     * @param: redissonClient
     * @Date 2025/1/5 21:19
     */
    public DistributeLockUtil(String key, Long tryLockTime, Long holdLockTime, RedissonClient redissonClient){
        this.key = key;
        this.tryLockTime = tryLockTime;
        this.holdLockTime = holdLockTime;
        this.redissonClient = redissonClient;
    }


    /***
     * @MethodName lock
     * @Description  加锁
     * @return: boolean
     * @Date 2025/1/5 15:49
     */
    public boolean lock() throws InterruptedException {
        RLock lock = redissonClient.getLock(key);

        return lock.tryLock(this.tryLockTime, this.holdLockTime,  TimeUnit.MILLISECONDS);
    }

    /***
     * @MethodName unLock
     * @Description  解锁
     * @Date 2025/1/5 15:49
     */
    public void unLock(){
        RLock lock = redissonClient.getLock(this.key);

        lock.unlock();
    }
}
