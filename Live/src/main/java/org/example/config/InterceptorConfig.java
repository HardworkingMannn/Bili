package org.example.config;

import org.example.interceptor.TokenInterceptor;
import org.example.ws.LiveSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Autowired
    private ApplicationContext applicationContext;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        TokenInterceptor bean = applicationContext.getBean(TokenInterceptor.class);
        registry.addInterceptor(bean).addPathPatterns("/**")
                .excludePathPatterns("/doc.html","/v3/api-docs","/v3/api-docs/swagger-config","/v3/api-docs/**","/swagger-ui/index.index","/webjars/**");
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        exporter.setAnnotatedEndpointClasses(LiveSocket.class);
        return exporter;
    }
}
