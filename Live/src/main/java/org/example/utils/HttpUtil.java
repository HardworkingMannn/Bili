package org.example.utils;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.example.config.HttpHeaderConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class HttpUtil {
    @Autowired
    private HttpHeaderConfigurationProperties header;
    private Random random;
    @PostConstruct
    public void construct(){
        random=new Random();
    }

    public String send(String url,String body) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        /*String url = "https://vcloud.163.com/app/channel/create";*/
        HttpPost httpPost = new HttpPost(url);

        String appKey = header.getAppKey();
        String appSecret = header.getAppSecret();
        String nonce =  random.nextLong()+"";
        String curTime = String.valueOf((new Date()).getTime() / 1000L);
        String checkSum = CheckSumBuilder.getCheckSum(appSecret, nonce ,curTime);//参考 计算CheckSum的java代码。

        // 设置请求的header。
        httpPost.addHeader("AppKey", appKey);
        httpPost.addHeader("Nonce", nonce);
        httpPost.addHeader("CurTime", curTime);
        httpPost.addHeader("CheckSum", checkSum);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");

        // 设置请求的参数。
        StringEntity params = new StringEntity(body, Consts.UTF_8);
        httpPost.setEntity(params);

        // 执行请求。
        HttpResponse response = httpClient.execute(httpPost);
        // 打印执行结果。
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "utf-8");
    }
    public String createLive(String name,Integer type) throws IOException {
        Map<String,Object> map=new HashMap<>();
        map.put("name",name);
        map.put("type",type);
        String jsonString = JSON.toJSONString(map);
        return send("https://vcloud.163.com/app/channel/create", jsonString);
    }
    public String reget(String cid) throws IOException {
        Map<String,Object> map=new HashMap<>();
        map.put("cid",cid);
        String jsonString = JSON.toJSONString(map);
        return send("https://vcloud.163.com/app/address", jsonString);
    }
}
