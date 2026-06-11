package dev.xing.infinitechat.ContactService.model.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InviteGroupRequest {

    @NotNull(message = "sessionId不能为空")
    private Long sessionId;

    @NotNull(message = "inviterId不能为空")
    private Long inviterId;

    @NotEmpty(message = "inviteeIds不能为空")
    private List<Long> inviteeIds;
}
