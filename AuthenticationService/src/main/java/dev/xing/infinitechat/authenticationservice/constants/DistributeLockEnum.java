package dev.xing.infinitechat.authenticationservice.constants;

/**
 * @EnumName DistributeLockEnum
 * @Description 分布式锁相关枚举类型
 * @Date 2025/1/5 14:46
 */

public enum DistributeLockEnum {
    // USER_REGISTER_PREFIX 用户注册分布式锁前缀
    USER_REGISTER_PREFIX("user_register_prefix_");


    // prefix 前缀
    private final String prefix;

    DistributeLockEnum(String prefix){
        this.prefix = prefix;
    }

    /***
     * @MethodName getPrefix
     * @Description  获取分布式锁前缀
     * @return: java.lang.String
     * @Date 2025/1/5 14:53
     */
    public String getPrefix(){
        return this.prefix;
    }

    /***
     * @MethodName toString
     * @Description 重写 toString 方法
     * @return: java.lang.String
     * @Date 2025/1/5 17:53
     */
    @Override
    public String toString() {
        return this.prefix;
    }
}