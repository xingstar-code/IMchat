package dev.xing.infinitechat.messagingservice.utils;


import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserLogoutListener {



    public static final AsyncCache<String, String> cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)  //写入一个key后，在指定时间后过期
            .expireAfterAccess(1, TimeUnit.MINUTES) //访问一个key后，在指定时间后过期
            .buildAsync();



    public void handleKeyDelete(String key) throws Exception{
        cache.synchronous().invalidate(key);
        log.info("用户登出，删除缓存key：" + key);

    }
}
