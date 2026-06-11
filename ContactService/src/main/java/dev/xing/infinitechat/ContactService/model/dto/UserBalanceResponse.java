package dev.xing.infinitechat.ContactService.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserBalanceResponse {

    private final BigDecimal balance;
}
