package dev.xing.infinitechat.ContactService.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.experimental.Accessors;

@Data
@TableName("apply_friend")
@Accessors(chain = true)
public class ApplyFriend {

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private Long targetId;

    private String msg;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
