package dev.xing.infinitechat.messagingservice.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

/**
 * 余额变动记录表
 */
@Data
@TableName("balance_log")
@Accessors(chain = true)
public class BalanceLog {

    /**
     * 记录ID
     */
    @TableId("balance_log_id")
    private Long balanceLogId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 变动金额，正数为增加，负数为减少
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 变动类型：1发送红包，2领取红包，3红包退回
     */
    @TableField("type")
    private Integer type;

    /**
     * 关联ID，如红包ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
