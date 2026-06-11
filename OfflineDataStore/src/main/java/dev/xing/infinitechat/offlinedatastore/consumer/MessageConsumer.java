package dev.xing.infinitechat.offlinedatastore.consumer;

import dev.xing.infinitechat.offlinedatastore.mapper.MessageMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.Message;
import dev.xing.infinitechat.offlinedatastore.model.entity.TextMessage;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final MessageMapper messageMapper;

    @KafkaListener(topics = "thousands_word_message", groupId = "thousands_word_message_all")
    public void listen(String message) {
        log.info("Received Message: " + message);
        TextMessage textMessage = JSONUtil.toBean(message, TextMessage.class);
        Message msg = new Message(); //message表
        BeanUtil.copyProperties(textMessage, msg);
        msg.setContent(textMessage.getBody().getContent());
        msg.setReplyId(textMessage.getBody().getReplyId());
        msg.setSenderId(textMessage.getSendUserId());

        log.info("Received Message: " + msg);
        messageMapper.insert(msg);
    }
}
