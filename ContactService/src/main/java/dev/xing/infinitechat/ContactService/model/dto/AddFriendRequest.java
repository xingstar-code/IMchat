package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddFriendRequest {

    private String msg;
}
