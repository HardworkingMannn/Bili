package org.example.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.example.utils.CheckSumBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Random;

@ConfigurationProperties(prefix = "wangyi")
@Component
@Data
public class HttpHeaderConfigurationProperties {
    private String appKey;
    private String appSecret;
}
