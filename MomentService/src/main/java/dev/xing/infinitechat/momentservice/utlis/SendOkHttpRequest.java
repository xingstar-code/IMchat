package dev.xing.infinitechat.momentservice.utlis;

import com.alibaba.fastjson.JSON;
import dev.xing.infinitechat.momentservice.common.ServiceException;
import dev.xing.infinitechat.momentservice.model.entity.Moment;
import dev.xing.infinitechat.momentservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.momentservice.model.enums.NoticeMomentEnum;
import dev.xing.infinitechat.momentservice.model.vo.*;
import dev.xing.infinitechat.momentservice.service.FriendService;
import dev.xing.infinitechat.momentservice.service.MomentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@SuppressWarnings({"all"})
@RequiredArgsConstructor
public class SendOkHttpRequest {
    // HTTP 请求

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MomentService momentService;

    @Autowired
    private FriendService friendService;

    private final DiscoveryClient discoveryClient;

    public void sendOkHttp(MomentRTCVO momentRTCVO, Long userId, Integer type, Long monentId) throws Exception{
        List<Long> friendIds = new ArrayList<>();
        Moment moment = momentService.getById(monentId);

        // type 为 1 是发朋友圈通知， 为 2 是 点赞通知
        if (type == NoticeMomentEnum.CREATE_MOMENT_NOTICE.getValue()){
            friendIds = friendService.getFriendIds(userId);
        } else {
            friendIds.add(moment.getUserId());
            if (moment.getUserId().equals(userId) ) {
                log.info("自己点赞评论朋友圈不通知");
                return;
            }
        }
        log.info(friendIds.toString());
        momentRTCVO.setReceiveUserIds(friendIds);
        List<ServiceInstance> instances = discoveryClient.getInstances("RealTimeCommunicationService");
        if (instances.size() == 0) {
            throw new ServiceException("没有 netty 服务");
        }
        log.info(momentRTCVO.toString());
        ExecutorService executorService = Executors.newFixedThreadPool(5); // 可根据需要调整线程数
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE = MediaType.get(ConfigEnum.MEDIA_TYPE.getValue());
        String token = stringRedisTemplate.opsForValue().get(userId.toString());
        String json = JSON.toJSONString(momentRTCVO);
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE, json);
        for (ServiceInstance instance : instances) {
            executorService.submit(() -> {
                String url = instance.getUri().toString();
                Request request = new Request.Builder()
                        .url(url + ConfigEnum.NOTICE_URL.getValue())
                        .post(requestBody)
                        .addHeader("Authorization", token)
                        .build();
                try {
                    client.newCall(request).execute();
                } catch (Exception e) {
                    log.error("发送消息失败", json, e);
                }
            });
        }
    }
}