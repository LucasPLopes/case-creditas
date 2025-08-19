package br.com.creditas.service.simulacao.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bulk")
public class BulkProperties {
    private Integer batchSize;
    private KafkaTopics kafka;
    private RabbitQueue rabbitmq;
    private Redis redis;


    @Data
    public static class KafkaTopics {
        private String request;
        private String response;

    }

    @Data
    public static class RabbitQueue {
        private String queue;
    }

    @Data
    public static class Redis {
        private long ttl;
    }
}