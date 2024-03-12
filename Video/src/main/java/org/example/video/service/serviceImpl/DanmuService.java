package org.example.video.service.serviceImpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.websocket.server.ServerEndpoint;
import org.example.Model.entity.Danmu;
import org.example.video.mapper.DanmuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DanmuService {
    @Autowired
    private DanmuMapper mapper;
    @Transactional
    public List<Danmu> selectList(String vid,long startTime,int index){
        List<Danmu> list = mapper.selectList(Wrappers.<Danmu>lambdaQuery().eq(Danmu::getVideoItemId, vid).ge(Danmu::getTime,System.currentTimeMillis()-startTime).orderByAsc(Danmu::getTime).last("limit "+index+" 10"));
        return list;
    }
}
