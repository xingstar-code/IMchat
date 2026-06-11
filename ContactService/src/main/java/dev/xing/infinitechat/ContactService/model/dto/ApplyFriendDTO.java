package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ApplyFriendDTO {

    private String userUuid;

    private String nickname;

    private String avatar;

    private String msg;

    private Integer status;

    private LocalDateTime time;

    /**
     * 是否是接受者。1 是，0 否
     */
    private Integer isReceiver;
}
