package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

/**
 * 请求 DTO，用于踢出群聊成员。
 */
@Data
@Accessors(chain = true)
public class KickGroupMembersRequest {

    /**
     * 群聊的 sessionId
     */
    private String sessionId;

    /**
     * 操作者的 userId
     */
    private Long operatorId;

    /**
     * 被移出者的 userId 列表
     */
    private List<Long> memberIds;
}
