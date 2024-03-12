package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.Model.entity.Mark;
import org.example.Model.pojo.MarkDTO;
import org.example.Model.pojo.PageResult;
import org.example.Model.pojo.ReplyDTO;
import org.example.Model.pojo.DongtaiDTO;
import org.example.entity.MarkRepresent;
import org.example.pojo.ReplyOutline;
import org.example.video.Model.pojo.Result;

import java.util.List;

public interface MarkService extends IService<Mark> {
    public Result publishDongtai(DongtaiDTO dto);
    public PageResult<List<MarkRepresent>> getReplyMark(String id, Integer pageNum, Integer pageSize, boolean heat);
    public PageResult<List<ReplyOutline>> getReply(String markId, Integer pageNum, Integer pageSize, boolean like);
    Result like(String markId);
    Result likeReply(String replyId);
    Result publishMark(MarkDTO dto);
    Result reply(ReplyDTO dto);
}
