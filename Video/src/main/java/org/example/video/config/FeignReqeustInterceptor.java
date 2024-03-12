package org.example.video.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FeignReqeustInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        //当feign 开启 hyxtr
        //当Feign开启Hystrix支持时， RequestContextHolder.getRequestAttributes 就会为null
        //原因在于 Hystrix的默认隔离策略是THREAD 。而 RequestContextHolder 源码中，使用了两个血淋淋的ThreadLocal 。
        //Hystrix官方强烈建议使用THREAD作为隔离策略
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //获取RequestContextHolder中的信息
        Map<String, String> headers = getHeaders(request);
        //放入feign的RequestTemplate中
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if(entry.getKey().equals("token")) {//只需要token就行
                System.out.println("令牌中继 机制 key:" + entry.getKey() + " value:" + entry.getValue());
                template.header(entry.getKey(), entry.getValue());
                break;
            }
        }

    }

    /**
     * 获取原请求头
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }
}
