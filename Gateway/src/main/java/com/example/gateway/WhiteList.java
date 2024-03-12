package com.example.gateway;

import jakarta.annotation.PostConstruct;
import jdk.jfr.DataAmount;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@ConfigurationProperties(prefix = "filter")
@Component
@Data
@Slf4j
public class WhiteList {
    private String[] pass;
    @PostConstruct
    public void construct(){
        log.info("创建白名单完成,白名单为：");
        log.info(Arrays.toString(pass));
    }
}
