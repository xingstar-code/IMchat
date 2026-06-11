package dev.xing.infinitechat.messagingservice.conf;

import dev.xing.infinitechat.messagingservice.common.ServiceException;
import dev.xing.infinitechat.messagingservice.utils.PreventDuplicateSubmit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AOP 切面类：用于防止重复提交
 */
@Slf4j
@Aspect
@Component
public class PreventDuplicateSubmitAspect {

    // 使用线程安全的 Map 存储请求的唯一键及其最近访问时间
    private final Map<String, Long> requestCache = new ConcurrentHashMap<>();

    // 监控带有注解的方法
    @Around("@annotation(preventDuplicateSubmit)")
    public Object preventDuplicate(ProceedingJoinPoint joinPoint,
                                   PreventDuplicateSubmit preventDuplicateSubmit) throws Throwable {

        // 根据方法名和参数生成请求的唯一键
        String key = joinPoint.getSignature().toShortString() + Arrays.toString(joinPoint.getArgs());

        long currentTime = System.currentTimeMillis(); // 当前时间戳
        Long lastRequestTime = requestCache.get(key);  // 获取上次请求的时间

        // 判断是否在超时时间内重复提交
        if (lastRequestTime != null && (currentTime - lastRequestTime) < preventDuplicateSubmit.timeout()) {
            log.warn("Duplicate submission detected for method: {}", key);
            throw new ServiceException("请勿重复提交请求！");
        }

        // 更新请求时间
        requestCache.put(key, currentTime);

        // 执行目标方法
        try {
            return joinPoint.proceed();
        } finally {
            // 可选：根据需要在一段时间后清理缓存
            // requestCache.remove(key);
        }
    }
}
