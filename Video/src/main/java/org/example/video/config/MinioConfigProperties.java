package org.example.video.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minio")
@Component
@Data
public class MinioConfigProperties {
    private String username;
    private String password;
    private String ip;
    private Integer port;
    private Integer access_port;
    private Integer ttl;
    private String path;
    private String access_path;
    @PostConstruct
    public void construct(){
        path="http://"+ip+":"+port;
        System.out.println(path);
        ttl*=1000*60*60;   //把小时转换成毫秒
    }
}
