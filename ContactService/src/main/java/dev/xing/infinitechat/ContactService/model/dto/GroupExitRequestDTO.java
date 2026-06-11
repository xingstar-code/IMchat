package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import lombok.experimental.Accessors;

/**
 * 群聊退出请求数据传输对象
 */
@Data
@Accessors(chain = true)
public class GroupExitRequestDTO {

    /**
     * 会话ID
     */
    @NotBlank(message = "sessionId不能为空")
    private Long sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "userId不能为空")
    private Long userId;
}
