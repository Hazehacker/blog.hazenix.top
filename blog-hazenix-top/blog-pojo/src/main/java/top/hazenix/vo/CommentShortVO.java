package top.hazenix.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentShortVO {
    private Long id;
    //评论者
    private String username;
    //评论内容
    private String content;
    //ToDO (不知道时间返回这个格式行不行)
    //评论时间
    private LocalDateTime createTime;

}
