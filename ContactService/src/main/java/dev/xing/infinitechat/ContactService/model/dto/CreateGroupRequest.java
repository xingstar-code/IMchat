package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateGroupRequest {

    private Long creatorId;

    private List<Long> memberIds;
}
