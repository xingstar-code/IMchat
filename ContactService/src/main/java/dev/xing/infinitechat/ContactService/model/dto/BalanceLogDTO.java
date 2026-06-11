package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BalanceLogDTO {

    private String userName;

    private Integer type;

    private String amount;

    private String time;
}
