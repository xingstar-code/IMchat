package dev.xing.infinitechat.messagingservice.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Data
@TableName("friend")
@Accessors(chain = true)
public class Friend {

    @TableId
    private Long id;

    private Long userId;

    private Long friendId;

    // 1: 好友, 2: 拉黑, 3: 删除
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
