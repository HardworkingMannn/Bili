package org.example.Model.constant;

import org.example.Model.entity.PartitionDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartitionConst {
    public final static Map<String, List<PartitionDTO>> map;
    static{
        map=new HashMap<>();
        ArrayList<PartitionDTO> list = new ArrayList<>();
        list.add(new PartitionDTO("MAD.AMV","具有一定创作度的动/静画二次创作视频"));
        list.add(new PartitionDTO("MMD.3D","使用MMD和其他3D软件制作的视频"));
        list.add(new PartitionDTO("短片·手书","追求个人特色和创意表达的动画短片及手书(绘)"));
        list.add(new PartitionDTO("配音","使用ACGN相关画面或台本素材进行人工配音创作的内容"));
        list.add(new PartitionDTO("MMD.3D","使用MMD和其他3D软件制作的视频"));
        list.add(new PartitionDTO("手办·模玩","手办模玩的测评、改造或某他衍生内容"));
        list.add(new PartitionDTO("特摄","特摄相关衍生视频"));
        map.put("推荐选择",list);
        list=new ArrayList<>(list);
        list.clear();
        list.add(new PartitionDTO("搞笑","搞笑挑战、剪辑、表演、配音以及各类日常沙雕视频"));
        list.add(new PartitionDTO("亲子","与萌娃、母婴、育儿相关的视频，包括但不限于萌娃日常、萌娃才艺、亲子互动、亲子教育、母婴经验分享、少儿用品分享等"));
        list.add(new PartitionDTO("出行","旅行、户外、本地探店相关的视频，如旅行v10g、治愈系风景、城市景点攻略、自驾游、户外露营、徒步、骑行、钓鱼、乡村游、本地探店体验，演出"));
        list.add(new PartitionDTO("三农","与农业、农村、农民相关的视频，包括但不限于农村生活、户外打野、种植技术、养殖技术、三农资讯"));
        list.add(new PartitionDTO("家居房产","与买房、装修、居家生活相关的视频，如买房租房、装修改造、智能家居、园艺绿植、居家好物等"));
        list.add(new PartitionDTO("手工","与手工艺、DIY、发明创造相关的视频，例如手工记录、四坑手作、毛毡粘土、手账文具、写字书法、模型、玩具、解压、传统手艺、非遗等"));
        map.put("生活",list);
        list=new ArrayList<>(list);
        list.clear();
        list.add(new PartitionDTO("单机游戏","以单机或其联机模式为主要内容的相关视频"));
        list.add(new PartitionDTO("网络游戏","多人在线游戏为主要内容的相关视频"));
        list.add(new PartitionDTO("手机游戏","手机及平板设备平台上的游戏相关视频"));
        list.add(new PartitionDTO("电子竞技","电子竟技游戏项目为主要内容的相关视频"));
        list.add(new PartitionDTO("桌游棋牌","桌游、棋牌、卡牌、聚会游戏等相关视频"));
        list.add(new PartitionDTO("音游","通过配合音乐与节奏而进行的音乐类游戏视频"));
        map.put("游戏",list);
        list=new ArrayList<>(list);
        list.clear();
        list.add(new PartitionDTO("综艺","所有综艺相关，全部一手掌握!"));
        list.add(new PartitionDTO("娱乐杂谈","娱乐人物解读、娱乐热点点评、娱乐行业分析"));
        list.add(new PartitionDTO("粉丝创作","粉丝向创作视频"));
        list.add(new PartitionDTO("明星综合","娱乐圈动态、明星资讯相关"));
        map.put("娱乐",list);
        list=new ArrayList<>(list);
        list.clear();
        list.add(new PartitionDTO("科学科普","以自然科学或基于自然科学思维展开的知识视频"));
        list.add(new PartitionDTO("社科·法律·心理","法律/心理/社会学/观点输出类内容等"));
        list.add(new PartitionDTO("人文历史","人物/文学/历史/文化/奇闻/艺术等"));
        list.add(new PartitionDTO("财经商业","财经/商业/经济金融/互联网等"));
        list.add(new PartitionDTO("校园学习","学习方法及经验、课程教学、校园干货分享等"));
        list.add(new PartitionDTO("职业职场","职场技能、职业分享、行业分析、求职规划等"));
        map.put("知识",list);
        list=new ArrayList<>(list);
        list.clear();
        list.add(new PartitionDTO("影视","杂谈影视评论、解说、吐槽、科普、配音等"));
        list.add(new PartitionDTO("影视剪辑","对影视素材进行剪辑再创作的视频"));
        list.add(new PartitionDTO("小剧场","单线或连续剧情，且有演绎成分的小剧场(短剧)内容"));
        list.add(new PartitionDTO("短片","各种类型的短片，包括但不限于真人故事短片、微电影、学生作品、创意短片、励志短片、广告短片、摄影短片、纪实短片、科幻短片等"));
        list.add(new PartitionDTO("预告·资讯","影视类相关资讯，预告，花絮等视频"));
        map.put("影视",list);
    }
}
