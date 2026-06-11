package dev.xing.infinitechat.momentservice.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateMomentDTO implements Serializable {

    private List<String> mediaUrls;

    private String text;

    private String userId;
}
