package dev.xing.infinitechat.authenticationservice.utils;

import dev.xing.infinitechat.authenticationservice.constants.DistributeLockEnum;
import lombok.Data;
import org.redisson.api.RedissonClient;

/**
 * @ClassName RegisterDistributeLock
 * @Description 注册分布式锁
 * @Date 2025/1/5 14:40
 */
@Data
public class RegisterDistributeLockUtil {
    // key 分布式锁的 key
    private String key;

    // tryLockTime 尝试获得锁的时间（单位：毫秒）
    private Long tryLockTime = 0L;

    // holdLockTime 持有锁时长（单位：毫秒）
    private Long holdLockTime = 1000 * 3L;

    // distributeLock 分布式锁
    private DistributeLockUtil distributeLock;

    /***
     * @MethodName RegisterDistributeLockUtil
     * @Description 构造器
     * @param: phone
     * @param: redissonClient
     * @Date 2025/1/5 21:18
     */
    public RegisterDistributeLockUtil(String phone, RedissonClient redissonClient){
        this.key = DistributeLockEnum.USER_REGISTER_PREFIX +  phone;
        this.distributeLock = new DistributeLockUtil(this.key,  this.tryLockTime, this.holdLockTime, redissonClient);
    }

    /***
     * @MethodName RegisterDistributeLockUtil
     * @Description 构造函数，key 和 尝试获取锁的时间为必传参数
     * @param: phone
     * @param: tryLockTime
     * @param: redissonClient
     * @Date 2025/1/5 21:18
     */
    public RegisterDistributeLockUtil(String phone, Long tryLockTime, RedissonClient redissonClient){
        this.key = DistributeLockEnum.USER_REGISTER_PREFIX + phone;
        this.tryLockTime = tryLockTime;
        this.distributeLock = new DistributeLockUtil(this.key,  this.tryLockTime, this.holdLockTime, redissonClient);
    }

    /***
     * @MethodName RegisterDistributeLockUtil
     * @Description 全参数构造器
     * @param: phone
     * @param: tryLockTime
     * @param: holdLockTime
     * @param: redissonClient
     * @Date 2025/1/5 21:18
     */
    public RegisterDistributeLockUtil(String phone, Long tryLockTime, Long holdLockTime, RedissonClient redissonClient){
        this.key = DistributeLockEnum.USER_REGISTER_PREFIX + phone;
        this.tryLockTime = tryLockTime;
        this.holdLockTime = holdLockTime;
        this.distributeLock = new DistributeLockUtil(this.key,  this.tryLockTime, this.holdLockTime, redissonClient);
    }

    /***
     * @MethodName lock
     * @Description 加锁
     * @return: boolean
     * @Date 2025/1/5 15:53
     */
    public boolean lock() throws InterruptedException {
        return this.distributeLock.lock();
    }

    /***
     * @MethodName unLock
     * @Description  解锁
     * @Date 2025/1/5 15:53
     */
    public void unLock(){
        this.distributeLock.unLock();
    }

}
