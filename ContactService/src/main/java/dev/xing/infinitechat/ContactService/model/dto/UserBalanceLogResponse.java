package dev.xing.infinitechat.ContactService.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class UserBalanceLogResponse {

    List<BalanceLogDTO> list;

    Integer total;
}
