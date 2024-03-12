package com.example.chatroom.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import redis.clients.jedis.Jedis;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;


@Configuration
public class Config {
    @Value("${sender.email}")
    private String emailSender;   //邮箱用户名
    @Value("${sender.password}")
    private String emailPassoword;  //邮箱密码
    @Value("${jedis.host}")
    private String jedisHost;
    @Value("${jedis.port}")
    private String jedisPort;

    @Bean
    @Scope("prototype")
    public Session session(){
        Properties props = new Properties();

        //	SMTP服务器连接信息
        //  126——smtp.126.com
        //  163——smtp.163.com
        //  qqsmtp.qq.com"
        props.put("mail.host", "smtp.163.com");//	SMTP主机名

        //  126——25
        //  163——645
        /*props.put("mail.smtp.port", "645");//	主机端口号*/
        props.put("mail.smtp.auth", "true");//	是否需要用户认证
        /*props.put("mail.smtp.starttls.enale", "true");//	启用TlS加密*/

        Session session = Session.getDefaultInstance(props,new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // TODO Auto-generated method stub
                return new PasswordAuthentication(emailSender,emailPassoword);
            }
        });
        //  控制台打印调试信息
        /*session.setDebug(true);*/
        return session;
    }
    @Bean
    public Pattern emailPattern(){//用于匹配邮箱的正则表达式
        return Pattern.compile("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$");
    }
    @Bean
    public Random random(){//随机数对象用于生成验证码
        return new Random();
    }
    @Bean
    public Jedis jedis(){
        return new Jedis(jedisHost,Integer.parseInt(jedisPort));
    }
    @Bean
    public DefaultKaptcha producer() {
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");
        properties.put("kaptcha.textproducer.font.color", "black");
        properties.put("kaptcha.textproducer.char.space", "10");
        properties.put("kaptcha.textproducer.char.length","4");
        properties.put("kaptcha.image.height","34");
        properties.put("kaptcha.image.width","138");
        properties.put("kaptcha.textproducer.font.size","25");

        properties.put("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        com.google.code.kaptcha.util.Config config = new com.google.code.kaptcha.util.Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
