package dev.xing.infinitechat.offlinedatastore.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic initialTopic(){
        return new NewTopic("thousands_word_message", 3 , (short) 3);
    }
}
