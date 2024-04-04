package org.example.video.config;

import com.rabbitmq.client.ConnectionFactory;
import org.example.video.constant.RabbitMQConst;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    @Autowired
    private MinioConfigProperties minioConfigProperties;
    @Autowired
    private RabbitMQConfigProperties rabbitMQConfigProperties;
    @Bean
    public Queue removeQueue(){
        return new Queue(RabbitMQConst.REMOVE_FILE_QUEUE_NAME);
    }
    @Bean
    public DirectExchange minioExchange(){
        return new DirectExchange(RabbitMQConst.MINIO_FILE_EXCHANGE_NAME);
    }
    @Bean
    public Binding removeBinding(Queue removeQueue,DirectExchange minioExchange){
        return BindingBuilder.bind(removeQueue).to(minioExchange).with(RabbitMQConst.REMOVE_FILE_QUEUE_KEY);
    }
    @Bean
    public Queue removeDelayedQueue(){
        Map<String,Object> args=new HashMap<>();
        args.put("x-message-ttl",minioConfigProperties.getTtl());
        args.put("x-dead-letter-exchange",RabbitMQConst.DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key",RabbitMQConst.DEAD_QUEUE_KEY);
        return new Queue(RabbitMQConst.REMOVE_FILE_DELAYED_QUEUE_NAME,true,false,false,args);
    }
    @Bean
    public Binding removeDelayedFileBinding(Queue removeDelayedQueue,DirectExchange minioExchange){
        return BindingBuilder.bind(removeDelayedQueue).to(minioExchange).with(RabbitMQConst.REMOVE_FILE_DELAYED_QUEUE_KEY);
    }
    @Bean
    public ConnectionFactory defaultRabbitConnectionFactory(){
        ConnectionFactory cachingConnectionFactory = new ConnectionFactory();
        cachingConnectionFactory.setHost(rabbitMQConfigProperties.getHost());
        cachingConnectionFactory.setPort(rabbitMQConfigProperties.getPort());
        cachingConnectionFactory.setUsername(rabbitMQConfigProperties.getUsername());
        cachingConnectionFactory.setPassword(rabbitMQConfigProperties.getPassword());
        cachingConnectionFactory.setVirtualHost("/");
        CorrelationData data = new CorrelationData();

        return cachingConnectionFactory;
    }
    @Bean
    public Queue removeDelay(){
        return new Queue(RabbitMQConst.REMOVE_DELAYED_QUEUE_NAME);
    }
    @Bean
    public Binding removeDelayBinding(Queue removeDelay,DirectExchange minioExchange){
        return BindingBuilder.bind(removeDelay).to(minioExchange).with(RabbitMQConst.REMOVE_DELAYED_QUEUE_KEY);
    }
    @Bean
    public Queue deadQueue(){
        return new Queue(RabbitMQConst.DEAD_QUEUE_NAME);
    }
    @Bean
    public DirectExchange deadExchange(){
        return new DirectExchange(RabbitMQConst.DEAD_EXCHANGE_NAME);
    }
    @Bean
    public Binding deadBinding(Queue deadQueue,DirectExchange deadExchange){
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(RabbitMQConst.DEAD_QUEUE_KEY);
    }
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue releaseQueue(){
        Map<String,Object> args=new HashMap<>();
        args.put("x-dead-letter-exchange",RabbitMQConst.DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key",RabbitMQConst.DEAD_RELEASE_QUEUE_KEY);
        return new Queue(RabbitMQConst.RELEASE_QUEUE_NAME,true,false,false,args);
    }
    @Bean
    public DirectExchange releaseExchange(){
        return new DirectExchange(RabbitMQConst.RELEASE_EXCHANGE_NAME);
    }
    @Bean
    public Binding releaseBinding(Queue releaseQueue,DirectExchange releaseExchange){
        return BindingBuilder.bind(releaseQueue).to(releaseExchange).with(RabbitMQConst.RELEASE_QUEUE_KEY);
    }
    @Bean
    public Queue deadReleaseQueue(){
        return new Queue(RabbitMQConst.DEAD_RELEASE_QUEUE_NAME);
    }
    @Bean
    public Binding deadReleaseBinding(Queue deadReleaseQueue,DirectExchange deadExchange){
        return BindingBuilder.bind(deadReleaseQueue).to(deadExchange).with(RabbitMQConst.DEAD_RELEASE_QUEUE_KEY);
    }
    @Bean
    public Queue danmuLoaderQueue(){
        return new Queue(RabbitMQConst.DANMU_LOADER_QUEUE_NAME,true,false,false);
    }
    @Bean
    public DirectExchange danmuLoaderExchange(){
        return new DirectExchange(RabbitMQConst.DANMU_LOADER_EXCHANGE_NAME);
    }
    @Bean
    public Binding danmuLoaderBinding(Queue danmuLoaderQueue,DirectExchange danmuLoaderExchange){
        return BindingBuilder.bind(danmuLoaderQueue).to(danmuLoaderExchange).with(RabbitMQConst.DANMU_LOADER_KEY);
    }

    @Bean
    public Queue danmuStoreQueue(){
        return new Queue(RabbitMQConst.DANMU_STORE_QUEUE_NAME,true,false,false);
    }
    @Bean
    public Binding danmuStoreBinding(Queue danmuStoreQueue,DirectExchange danmuLoaderExchange){
        return BindingBuilder.bind(danmuStoreQueue).to(danmuLoaderExchange).with(RabbitMQConst.DANMU_STORE_KEY);
    }


}
