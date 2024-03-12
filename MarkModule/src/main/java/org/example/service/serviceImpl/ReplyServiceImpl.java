package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.Model.entity.Mark;
import org.example.Model.entity.Reply;
import org.example.mapper.MarkMapper;
import org.example.mapper.ReplyMapper;
import org.example.service.MarkService;
import org.example.service.ReplyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyService {

}
