package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FriendDTO {

    private String userUuid;

    private String nickname;

    private String avatar;

    private String SessionId;
}
