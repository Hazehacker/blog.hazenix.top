package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsVO {


//    private Long userId;
    //评论者名称
    private String username;
//    private Long replyId;
    //被评论者名称
    private String replyUsername;
    //评论内容
    private String content;
    //评论点赞数
    private Integer likeCount;
    //评论时间
    private String createTime;
}
