package org.example.service;

import org.example.Model.pojo.LikeDetails;
import org.example.Model.pojo.LikeNotifyOutline;
import org.example.Model.pojo.ReplyNotifyVO;
import org.example.video.Model.pojo.Result;

import java.util.List;

public interface NotifyService {
    public Result<List<LikeDetails>> getLikeDetails(Integer pageNum, Integer pageSize, String id, Integer likeType);
    public Result<List<LikeNotifyOutline>> getLikeNotify(Integer pageNum, Integer pageSize);
    public Result<List<ReplyNotifyVO>> getReplyNotify(Integer pageNum, Integer pageSize);
    public Result getCall(Integer pageNum, Integer pageSize);
}
