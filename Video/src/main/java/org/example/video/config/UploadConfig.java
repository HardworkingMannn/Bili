package org.example.video.config;

import io.minio.MinioClient;
import org.example.video.Model.constant.JedisConst;
import org.example.video.interceptor.TokenInterceptor;
import org.example.video.ws.DanmuSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import redis.clients.jedis.Jedis;

@Configuration
public class UploadConfig implements WebMvcConfigurer {
    @Autowired
    private MinioConfigProperties configProperties;
    @Autowired
    private ApplicationContext applicationContext;
    @Bean
    public MinioClient client(){
        return MinioClient.builder().credentials(configProperties.getUsername(), configProperties.getPassword())
                .endpoint(configProperties.getPath())
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        TokenInterceptor bean = applicationContext.getBean(TokenInterceptor.class);
        registry.addInterceptor(bean).addPathPatterns("/**")
                .excludePathPatterns("/doc.html","/v3/api-docs","/v3/api-docs/swagger-config","/v3/api-docs/**","/swagger-ui/index.index","/webjars/**"
                ,"/video/img","/video/getHomePageVideo","/video/danmu/**","/video/num/**");
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        exporter.setAnnotatedEndpointClasses(DanmuSocket.class);
        return exporter;
    }
    @Bean
    public Jedis jedis(){
        return new Jedis(JedisConst.JEDIS_HOST,JedisConst.JEDIS_PORT);
    }
}
