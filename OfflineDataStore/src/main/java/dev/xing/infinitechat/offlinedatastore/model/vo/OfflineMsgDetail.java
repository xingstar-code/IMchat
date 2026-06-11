package dev.xing.infinitechat.offlinedatastore.model.vo;

import lombok.Data;
import java.io.Serializable;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OfflineMsgDetail implements Serializable {

    private String avatar;

    private Body body;

    private Integer type;

    private String userName;

    private String sendUserId;

    private String messageId;
}
