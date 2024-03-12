package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CollectorContent;
import org.example.mapper.CollectorContentMapper;
import org.example.service.CollectorContentService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CollectorContentServiceImpl extends ServiceImpl<CollectorContentMapper, CollectorContent> implements CollectorContentService {
}
