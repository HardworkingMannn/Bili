package org.example.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix="spring.rabbitmq")
@Component
public class RabbitMQConfigProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
}
