package dev.xing.infinitechat.messagingservice.model.dto;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SendMsgRequest implements Serializable {

    private Long sessionId;

    private Long sendUserId;

    private Integer sessionType;

    private Integer type;

    private Long receiveUserId;

    // private String messageUuid;
    private Object body;
}
