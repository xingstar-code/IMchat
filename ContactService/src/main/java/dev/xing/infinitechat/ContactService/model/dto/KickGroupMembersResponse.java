package dev.xing.infinitechat.ContactService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

/**
 * 响应 DTO，包含成功移出的用户 ID 列表。
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class KickGroupMembersResponse {

    /**
     * 成功移出的 userId 列表
     */
    private List<String> successIds;
}
