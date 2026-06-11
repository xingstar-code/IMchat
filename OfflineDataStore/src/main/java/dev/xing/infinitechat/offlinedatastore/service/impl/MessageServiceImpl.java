package dev.xing.infinitechat.offlinedatastore.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xing.infinitechat.offlinedatastore.mapper.MessageMapper;
import dev.xing.infinitechat.offlinedatastore.mapper.RedPacketMapper;
import dev.xing.infinitechat.offlinedatastore.model.entity.Message;
import dev.xing.infinitechat.offlinedatastore.model.entity.RedPacket;
import dev.xing.infinitechat.offlinedatastore.model.entity.RedPacketMessageBody;
import dev.xing.infinitechat.offlinedatastore.model.entity.User;
import dev.xing.infinitechat.offlinedatastore.model.vo.Body;
import dev.xing.infinitechat.offlinedatastore.model.vo.OfflineMsgDetail;
import dev.xing.infinitechat.offlinedatastore.service.MessageService;
import dev.xing.infinitechat.offlinedatastore.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2024-09-20 16:39:30
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {


    @Autowired
    private UserService userService;

    @Autowired
    private RedPacketMapper redPacketMapper;

    @Override
    public List<OfflineMsgDetail> findOfflineMsgBySessionId(Long sessionId, String time) {
        LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId).gt("created_at", dateTime);
        List<Message> messages = this.baseMapper.selectList(queryWrapper);
        log.info("messages:{}", messages);
        List<OfflineMsgDetail> offlineMsgDetails = new ArrayList<>();
        for (Message message : messages) {
            OfflineMsgDetail offlineMsgDetail = new OfflineMsgDetail();
            Body body = new Body();
            Date date = message.getCreatedAt();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            body.setCreatedAt(formatter.format(date));
            body.setContent(message.getContent());
            if (message.getReplyId() != null) {
                body.setReplyId(message.getReplyId().toString());
            }
            offlineMsgDetail.setBody(body);

            //如果是红包则查询红包封面并返回
            if (message.getType() == 5) {
                RedPacket redPacket = redPacketMapper.selectById(message.getContent());
                RedPacketMessageBody body1 = new RedPacketMessageBody(redPacket.getRedPacketWrapperText());
                BeanUtils.copyProperties(body, body1);
                offlineMsgDetail.setBody(body1);
            }




            User user = userService.getById(message.getSenderId());
            offlineMsgDetail.setUserName(user.getUserName());
            offlineMsgDetail.setAvatar(user.getAvatar());
            offlineMsgDetail.setMessageId(message.getMessageId().toString());
            offlineMsgDetail.setSendUserId(message.getSenderId().toString());
            offlineMsgDetail.setType(message.getType());
            offlineMsgDetails.add(offlineMsgDetail);
            log.info("offlineMsgDetail:{}", offlineMsgDetail);

        }
        return offlineMsgDetails;
    }
}




