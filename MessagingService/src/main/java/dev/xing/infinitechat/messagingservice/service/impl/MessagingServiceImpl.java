package dev.xing.infinitechat.messagingservice.service.impl;

import javax.annotation.PreDestroy;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.messagingservice.common.ServiceException;
import dev.xing.infinitechat.messagingservice.mapper.FriendMapper;

import dev.xing.infinitechat.messagingservice.mapper.MessageMapper;
import dev.xing.infinitechat.messagingservice.model.dto.SendMsgRequest;
import dev.xing.infinitechat.messagingservice.model.entity.*;
import dev.xing.infinitechat.messagingservice.model.enums.ConfigEnum;
import dev.xing.infinitechat.messagingservice.model.enums.SessionType;
import dev.xing.infinitechat.messagingservice.model.vo.KafkaMsgVO;
import dev.xing.infinitechat.messagingservice.model.vo.ResponseMsgVo;
import dev.xing.infinitechat.messagingservice.service.MessagingService;
import com.alibaba.fastjson.JSON;
import dev.xing.infinitechat.messagingservice.service.SessionService;
import dev.xing.infinitechat.messagingservice.service.UserService;
import dev.xing.infinitechat.messagingservice.service.UserSessionService;
import dev.xing.infinitechat.messagingservice.utils.UserLogoutListener;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * 消息发送服务实现类
 */
@Service
@Slf4j
public class MessagingServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessagingService {

    private static final int STATUS_ACTIVE = 1;
    private static final String DEFAULT_SESSION_AVATAR = "http://localhost:8080/img/avatar/IM_GROUP.jpg";
    private static final String TIME_ZONE_SHANGHAI = "Asia/Shanghai";

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 60L; // 60秒
    private static final int QUEUE_CAPACITY = 100;

    private final UserService userService;
    private final FriendMapper friendMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserSessionService userSessionService;
    private final UserLogoutListener userLogoutListener;
    private final SessionService sessionService;
    private final DiscoveryClient discoveryClient;

    private final OkHttpClient httpClient = new OkHttpClient();

    private final ThreadPoolExecutor groupMessageExecutor;

    // 构造函数
    public MessagingServiceImpl(UserService userService,
                                FriendMapper friendMapper,
                                KafkaTemplate<String, String> kafkaTemplate,
                                RedisTemplate<String, String> redisTemplate,
                                UserSessionService userSessionService,
                                UserLogoutListener userLogoutListener,
                                SessionService sessionService,
                                DiscoveryClient discoveryClient) {
        this.userService = userService;
        this.friendMapper = friendMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.userSessionService = userSessionService;
        this.userLogoutListener = userLogoutListener;
        this.sessionService = sessionService;
        this.discoveryClient = discoveryClient;

        this.groupMessageExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public ResponseMsgVo sendMessage(SendMsgRequest sendMsgRequest) {
        log.info("发送消息请求: {}", sendMsgRequest);

        validateSender(sendMsgRequest.getSendUserId());

        List<Long> receiveUserIds = getReceiveUserIds(sendMsgRequest);

        validateReceiveUserIds(receiveUserIds);

        AppMessage appMessage = buildAppMessage(sendMsgRequest, receiveUserIds);

        Long messageId = generateMessageId();
        Date createdAt = new Date();
        sendKafkaMessage(sendMsgRequest, sendMsgRequest.getSendUserId(), messageId, createdAt);

        appMessage.setMessageId(messageId);
        appMessage.setCreatedAt(formatDate(createdAt));

        sendRealTimeMessage(sendMsgRequest, appMessage, createdAt);

        return buildResponseMsgVo(appMessage);
    }

    /**
     * 验证发送者的状态
     *
     * @param sendUserId 发送者用户ID
     */
    private void validateSender(Long sendUserId) {
        User senderUser = userService.getById(sendUserId);
        log.info("发送者状态: {}", sendUserId);
        if (senderUser == null || senderUser.getStatus() != STATUS_ACTIVE) {
            throw new ServiceException("发送者状态异常");
        }
    }

    /**
     * 获取接收者用户ID列表
     *
     * @param sendMsgRequest 发送消息请求
     * @return 接收者用户ID列表
     */
    private List<Long> getReceiveUserIds(SendMsgRequest sendMsgRequest) {
        List<Long> receiveUserIds = new ArrayList<>();
        int sessionType = sendMsgRequest.getSessionType();

        if (sessionType == SessionType.SINGLE.getValue()) {
            Long receiveUserId = sendMsgRequest.getReceiveUserId();
            receiveUserIds.add(receiveUserId);
            validateSingleSession(sendMsgRequest.getSendUserId(), receiveUserId);
        } else {
            receiveUserIds.addAll(userSessionService.getUserIdsBySessionId(sendMsgRequest.getSessionId()));
            log.info("群聊接收者列表: {}", receiveUserIds);
            boolean removed = receiveUserIds.remove(sendMsgRequest.getSendUserId());
            if (removed) {
                log.info("移除发送者后的接收者列表: {}", receiveUserIds);
            } else {
                throw new ServiceException("发送者不在群聊内");
            }
        }

        return receiveUserIds;
    }

    /**
     * 验证单聊会话中的接收者状态和好友关系
     *
     * @param sendUserId    发送者用户ID
     * @param receiveUserId 接收者用户ID
     */
    private void validateSingleSession(Long sendUserId, Long receiveUserId) {
        User receiverUser = userService.getById(receiveUserId);
        if (receiverUser == null || receiverUser.getStatus() != STATUS_ACTIVE) {
            throw new ServiceException("接收者 " + receiveUserId + " 状态异常");
        }

        Friend friend = friendMapper.selectFriendship(sendUserId, receiveUserId);
        log.info("发送者ID: {}, 接收者ID: {}", sendUserId, receiveUserId);
        if (friend == null || friend.getStatus() != STATUS_ACTIVE) {
            throw new ServiceException("发送者 " + sendUserId + " 与接收者 " + receiveUserId + " 不是好友关系");
        }
    }

    /**
     * 验证接收者列表是否为空
     *
     * @param receiveUserIds 接收者用户ID列表
     */
    private void validateReceiveUserIds(List<Long> receiveUserIds) {
        if (receiveUserIds == null || receiveUserIds.isEmpty()) {
            throw new ServiceException("接收者列表不能为空");
        }
    }

    /**
     * 构建AppMessage对象
     *
     * @param sendMsgRequest 发送消息请求
     * @param receiveUserIds 接收者用户ID列表
     * @return 构建后的AppMessage对象
     */
    private AppMessage buildAppMessage(SendMsgRequest sendMsgRequest, List<Long> receiveUserIds) {
        AppMessage appMessage = new AppMessage();
        BeanUtils.copyProperties(sendMsgRequest, appMessage);
        appMessage.setBody(sendMsgRequest.getBody());
        appMessage.setReceiveUserIds(receiveUserIds);

        User senderUser = userService.getById(sendMsgRequest.getSendUserId());
        appMessage.setAvatar(senderUser.getAvatar());
        appMessage.setUserName(senderUser.getUserName());

        Session session = sessionService.getById(sendMsgRequest.getSessionId());
        log.info("会话ID: {}", sendMsgRequest.getSessionId());
        log.info("会话信息: {}", session);

        if (appMessage.getSessionType() == SessionType.SINGLE.getValue()) {
            appMessage.setSessionAvatar(null);
            appMessage.setSessionName(null);
        } else {
            appMessage.setSessionAvatar(DEFAULT_SESSION_AVATAR);
            appMessage.setSessionName(session.getName());
        }

        log.info("AppMessage: {}", appMessage);
        return appMessage;
    }

    /**
     * 生成消息ID
     *
     * @return 生成的消息ID
     */
    private Long generateMessageId() {
        Snowflake snowflake = IdUtil.getSnowflake(
                Integer.parseInt(ConfigEnum.WORKED_ID.getValue()),
                Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue())
        );
        return snowflake.nextId();
    }

    /**
     * 格式化日期
     *
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_SHANGHAI));
        return formatter.format(date);
    }

    /**
     * 发送Kafka消息
     *
     * @param sendMsgRequest 发送消息请求
     * @param sendUserId     发送者用户ID
     * @param messageId      消息ID
     * @param createdAt      创建时间
     */
    private void sendKafkaMessage(SendMsgRequest sendMsgRequest, Long sendUserId, Long messageId, Date createdAt) {
        KafkaMsgVO kafkaMsgVO = new KafkaMsgVO();
        BeanUtils.copyProperties(sendMsgRequest, kafkaMsgVO);
        kafkaMsgVO.setMessageId(messageId);
        kafkaMsgVO.setCreateAt(createdAt);

        String kafkaJSON = JSON.toJSONString(kafkaMsgVO);
        log.info("发送Kafka消息: {}", kafkaJSON);

        kafkaTemplate.send(ConfigEnum.KAFKA_TOPICS.getValue(), sendUserId.toString(), kafkaJSON)
                .addCallback(result -> log.info("Kafka消息发送成功: {}", result.getRecordMetadata()),
                        ex -> log.error("Kafka消息发送失败: {}", ex.getMessage()));
    }

    /**
     * 发送实时消息到RealTimeCommunicationService
     *
     * @param sendMsgRequest 发送消息请求
     * @param appMessage     应用消息对象
     * @param createdAt      创建时间
     */
    private void sendRealTimeMessage(SendMsgRequest sendMsgRequest, AppMessage appMessage, Date createdAt) {
        String json = JSON.toJSONString(appMessage);
        String token = redisTemplate.opsForValue().get(String.valueOf(sendMsgRequest.getSendUserId()));
        RequestBody requestBody = RequestBody.create(
                MediaType.parse(ConfigEnum.MEDIA_TYPE.getValue()),
                json
        );

        List<ServiceInstance> instances = discoveryClient.getInstances("RealTimeCommunicationService");
        if (instances.isEmpty()) {
            throw new ServiceException("没有可用的RealTimeCommunicationService服务实例");
        }

        if (sendMsgRequest.getSessionType() == SessionType.SINGLE.getValue()) {
            sendSingleMessage(sendMsgRequest, requestBody, token);
        } else {
            sendGroupMessage(instances, requestBody, token);
        }
    }

    /**
     * 发送单聊消息
     *
     * @param sendMsgRequest 发送消息请求
     * @param requestBody    HTTP请求体
     * @param token          授权令牌
     */
    private void sendSingleMessage(SendMsgRequest sendMsgRequest, RequestBody requestBody, String token) {
        String receiveUserId = String.valueOf(sendMsgRequest.getReceiveUserId());
        String nettyUri = "Nacos:" + receiveUserId;

        CompletableFuture<String> urlFuture = userLogoutListener.cache.get(nettyUri,
                key -> redisTemplate.opsForValue().get(nettyUri));
        log.info("单聊接收者ID: {}, Netty URI: {}", receiveUserId, nettyUri);

        try {
            String url = urlFuture.get();
            if (url != null) {
                Request request = new Request.Builder()
                        .url(url + ConfigEnum.MSG_URL.getValue())
                        .post(requestBody)
                        .addHeader("Authorization", token)
                        .build();
                executeHttpRequest(request);
            } else {
                log.info("接收者已下线: {}", receiveUserId);
            }
        } catch (Exception e) {
            log.error("发送单聊消息失败: {}", e.getMessage());
            throw new ServiceException("发送单聊消息失败");
        }
    }

    /**
     * 发送群聊消息
     *
     * @param instances    RealTimeCommunicationService服务实例列表
     * @param requestBody  HTTP请求体
     * @param token        授权令牌
     */
    private void sendGroupMessage(List<ServiceInstance> instances, RequestBody requestBody, String token) {
        for (ServiceInstance instance : instances) {
            groupMessageExecutor.submit(() -> {
                String url = instance.getUri().toString();
                Request request = new Request.Builder()
                        .url(url + ConfigEnum.MSG_URL.getValue())
                        .post(requestBody)
                        .addHeader("Authorization", token)
                        .build();
                try {
                    executeHttpRequest(request);
                    log.info("成功发送群聊消息到 {}", url);
                } catch (Exception e) {
                    log.error("发送群聊消息到 {} 失败: {}", url, e.getMessage());
                    // 根据需求，可以在此处添加重试机制或其他错误处理逻辑
                }
            });
        }
    }


    /**
     * 执行HTTP请求
     *
     * @param request HTTP请求对象
     * @throws IOException 如果请求失败
     */
    private void executeHttpRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP请求失败: " + response);
            }
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseString = responseBody.string();
                // 处理响应内容（根据业务需求）
                log.info("HTTP响应: {}", responseString);
            }
        }
    }

    /**
     * 构建响应消息对象
     *
     * @param appMessage 应用消息对象
     * @return 响应消息对象
     */
    private ResponseMsgVo buildResponseMsgVo(AppMessage appMessage) {
        ResponseMsgVo responseMsgVo = new ResponseMsgVo();
        BeanUtils.copyProperties(appMessage, responseMsgVo);
        responseMsgVo.setSessionId(String.valueOf(appMessage.getSessionId()));
        responseMsgVo.setCreatedAt(appMessage.getCreatedAt());

        log.info("消息 appMessage: {}", appMessage);
        log.info("消息 responseMsgVo: {}", responseMsgVo);
        return responseMsgVo;
    }


    @PreDestroy
    public void shutdownExecutor() {
        log.info("正在关闭 groupMessageExecutor...");
        groupMessageExecutor.shutdown();
        try {
            if (!groupMessageExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("groupMessageExecutor 未能在指定时间内终止。");
                groupMessageExecutor.shutdownNow();
            }
            log.info("groupMessageExecutor 关闭完成。");
        } catch (InterruptedException e) {
            log.error("在关闭 groupMessageExecutor 时被中断。", e);
            groupMessageExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}