package dev.xing.infinitechat.realtimecommunicationservice.module.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HeartBody implements Serializable {

    private Integer type;

    private String message;
}
