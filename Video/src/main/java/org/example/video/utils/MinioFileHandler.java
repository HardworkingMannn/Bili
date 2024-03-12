package org.example.video.utils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.example.video.constant.RabbitMQConst;
import org.example.video.entity.FileBelong;
import org.example.video.mapper.FileBelongMapper;
import org.example.video.pojo.RemoveDelayed;
import org.example.video.pojo.RemoveDelayedFile;
import org.example.video.pojo.RemoveFile;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MinioFileHandler {
    @Autowired
    private MinioClient client;
    @Autowired
    private FileBelongMapper belongMapper;
    @Autowired
    private ConnectionFactory connectionFactory;
    @RabbitListener(queues={RabbitMQConst.REMOVE_FILE_QUEUE_NAME})
    public void removeFile(RemoveFile removeFile){
        log.info("触发删除分块文件的mq处理{}",removeFile);
        for (int i = 0; i < removeFile.getChunkTotal(); i++) {
            File file = new File(removeFile.getFolder() + removeFile.getFilename() + i + ".temp");
            if(file.exists()){
                file.delete();
            }
        }
        File file = new File(removeFile.getFolder() + removeFile.getFilename() + "."+removeFile.getExtension());
        if(file.exists()){
            file.delete();
        }
    }
    @RabbitListener(queues={RabbitMQConst.DEAD_QUEUE_NAME})   //删除过期文件
    public void removeDelayedFile(RemoveDelayedFile removeFile){
        log.info("删除过期文件任务{}",removeFile);
        FileBelong fileBelong = belongMapper.selectOne(Wrappers.<FileBelong>lambdaQuery().eq(FileBelong::getUserId,removeFile.getUserId()).eq(FileBelong::getFilename,removeFile.getFilename()));
        if(fileBelong.getStatus()){
            return;
        }
        log.info("触发删除过期未合并的文件的mq处理{}",removeFile);
        File folder = new File(removeFile.getFolder());
        File[] files = folder.listFiles();
        for(File file:files){
            String name = file.getName();
            if(name.startsWith(removeFile.getFilename())) {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
    @RabbitListener(queues={RabbitMQConst.REMOVE_DELAYED_QUEUE_NAME})
    public void removeDelayedMission(RemoveDelayed removeDelayed) throws IOException, TimeoutException {
        log.info("触发删除删除文件任务的mq处理{}",removeDelayed);
        Channel channel = connectionFactory.newConnection().createChannel();
        boolean delete=false;
        while(!delete) {
            GetResponse getResponse = channel.basicGet(RabbitMQConst.REMOVE_FILE_DELAYED_QUEUE_NAME, false);
            if(getResponse==null){
                break;
            }
            String json = new String(getResponse.getBody(), "UTF-8");
            RemoveDelayedFile file = JSON.parseObject(json, RemoveDelayedFile.class);
            if(file.getUserId()==removeDelayed.getUserId()&&removeDelayed.getFilename().equals(file.getFilename())){
                channel.basicAck(getResponse.getEnvelope().getDeliveryTag(),false);
                delete=true;
            }else{
                channel.basicNack(getResponse.getEnvelope().getDeliveryTag(),false,true);
            }
        }
        channel.close();
    }


}
