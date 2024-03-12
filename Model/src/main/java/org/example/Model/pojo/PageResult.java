package org.example.Model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.video.Model.pojo.Result;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T>{
    private Integer code;
    private String message;
    private Record<T> data;
    private Boolean isEnd;

    public static PageResult success(){
        return new PageResult(200,"success",null,false);
    }
    public static <T> PageResult success(T t,Long total,Integer pageNum,Integer pageSize){
        return new PageResult(200,"success",new Record(t,total,pageNum,pageSize),false);
    }
    public static PageResult fail(String message){
        return new PageResult(0,message,null,false);
    }
    public static PageResult end(){return new PageResult(200,"success",null,true);}
    public static <T> PageResult success(T t,Long total,Integer pageNum,Integer pageSize,Boolean isEnd){
        return new PageResult(200,"success",new Record(t,total,pageNum,pageSize),isEnd);
    }

}
