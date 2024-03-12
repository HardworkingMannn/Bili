package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Fans;
import org.example.entity.History;
import org.example.mapper.FansMapper;
import org.example.mapper.HistoryMapper;
import org.example.service.FansService;
import org.example.service.HistoryService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements HistoryService {
}
