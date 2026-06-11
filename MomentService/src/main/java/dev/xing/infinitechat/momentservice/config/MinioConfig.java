package dev.xing.infinitechat.momentservice.config;



import dev.xing.infinitechat.momentservice.model.enums.ConfigEnum;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class MinioConfig {


    @Bean
    public MinioClient buildMinioClient() {
        return MinioClient
                .builder()
                .credentials(ConfigEnum.MINIO_ACCESS_KEY.getValue(), ConfigEnum.MINIO_SECRET_KEY.getValue())
                .endpoint(ConfigEnum.MINIO_SERVER_URL.getValue())
                .build();
    }
}

