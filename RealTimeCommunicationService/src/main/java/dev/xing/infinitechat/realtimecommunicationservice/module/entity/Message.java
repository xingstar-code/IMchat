package dev.xing.infinitechat.realtimecommunicationservice.module.entity;

import com.alibaba.fastjson.annotation.JSONType;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Message {

    protected String sessionId;

    protected List<Long> receiveUserIds;

    protected String sendUserId;

    protected String avatar;

    protected String userName;

    protected Integer type;

    protected String messageId;

    protected Integer sessionType;

    protected String sessionName;

    protected String sessionAvatar;

    protected String createdAt;

    protected Object body;
}
