package dev.xing.infinitechat.messagingservice.model.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppMessage {

    protected Long sessionId;

    protected List<Long> receiveUserIds;

    protected Long sendUserId;

    protected String userName;

    protected String avatar;

    protected Integer type;

    protected Long messageId;

    protected Integer sessionType;

    protected String sessionName;

    protected String sessionAvatar;

    private String createdAt;

    protected Object body;
}
