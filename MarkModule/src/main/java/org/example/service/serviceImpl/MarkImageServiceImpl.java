package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.Model.entity.Mark;
import org.example.Model.entity.MarkImage;
import org.example.mapper.MarkImageMapper;
import org.example.mapper.MarkMapper;
import org.example.service.MarkImageService;
import org.example.service.MarkService;
import org.springframework.stereotype.Service;

@Service
public class MarkImageServiceImpl extends ServiceImpl<MarkImageMapper, MarkImage> implements MarkImageService {
}
