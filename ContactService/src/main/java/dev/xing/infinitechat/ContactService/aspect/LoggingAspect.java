package dev.xing.infinitechat.ContactService.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * AOP切面类记录整体项目请求和响应信息
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    //拦截所有被@GetMapping、@PostMapping以及@RequestMapping等注解修饰的方法
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")

    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求的URI地址
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        String uri = servletRequestAttributes.getRequest().getRequestURI();

        // 获取方法名和参数
        Object[] args = joinPoint.getArgs();

        // 记录入参
        logger.info("调用接口，URL地址：{}，入参：{}", uri, Arrays.toString(args));

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 调用目标方法
        Object result = joinPoint.proceed();

        // 记录请求结束时间，并计算处理时间
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 记录返回结果和处理时间
        logger.info("响应接口，URL地址：{}，处理时间(ms)：{}，响应结果：{}", uri, duration, result);

        return result;
    }
}
