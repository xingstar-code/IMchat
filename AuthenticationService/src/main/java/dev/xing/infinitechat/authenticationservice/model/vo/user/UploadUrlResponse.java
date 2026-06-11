package dev.xing.infinitechat.authenticationservice.model.vo.user;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName UploadUrlResponse
 * @Description 预授权上传文件响应信息
 * @Date 2025/1/12 00:46
 */

@Data
@Accessors(chain = true)
public class UploadUrlResponse {
    // 上传文件的地址
    public String uploadUrl;

    // 下载文件的地址
    public String downloadUrl;
}
