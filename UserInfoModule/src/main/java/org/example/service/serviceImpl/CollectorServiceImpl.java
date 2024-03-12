package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.entity.Collector;
import org.example.mapper.CollectorMapper;
import org.example.service.CollectorService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CollectorServiceImpl extends ServiceImpl<CollectorMapper, Collector> implements CollectorService {
}
