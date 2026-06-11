package dev.xing.infinitechat.authenticationservice.utils;

import java.io.InputStream;

import dev.xing.infinitechat.authenticationservice.model.enums.ConfigEnum;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UpdateAvatarUtil {

    private final MinioClient minioClient;


    // 上传头像并返回访问URL
    public String updateAvatar(MultipartFile file) throws Exception {
        String fileSuffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        Snowflake snowflake = IdUtil.getSnowflake(Integer.parseInt(ConfigEnum.WORKED_ID.getValue()), Integer.parseInt(ConfigEnum.DATACENTER_ID.getValue()));
        String id = snowflake.nextIdStr();
        String fileName = id + "." + fileSuffix;
        InputStream inputStream = file.getInputStream();
        String contentType = new ContentTypeUtil().getType(fileSuffix);
        minioClient.putObject(PutObjectArgs.builder().bucket(ConfigEnum.MINIO_BUCKET_NAME.getValue())
                .object(fileName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(contentType)
                .build());
        String url = ConfigEnum.IMAGE_URI.getValue() + fileName;
        return url;
    }
}
