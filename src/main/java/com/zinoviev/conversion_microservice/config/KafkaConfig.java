package com.zinoviev.conversion_microservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final Environment env;

    @Bean
    NewTopic createConversionCreateEventsTopic() {
        return TopicBuilder.name(env.getProperty("topic.conversion.created.events"))
                .partitions(1)
                .build();
    }

    @Bean
    NewTopic createConversionProcessedEventsTopic() {
        return TopicBuilder.name(env.getProperty("topic.conversion.processed.events"))
                .partitions(1)
                .build();
    }


}
