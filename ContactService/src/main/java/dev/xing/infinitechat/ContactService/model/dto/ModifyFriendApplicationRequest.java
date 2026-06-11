package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModifyFriendApplicationRequest {

    private List<String> receiveUserUuids;
}
