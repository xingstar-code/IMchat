package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateGroupResponse {

    private String sessionId;

    private String sessionName;

    private Integer sessionType;

    private String avatar;

    private String creatorId;

    private List<String> failedMemberIds;
}
