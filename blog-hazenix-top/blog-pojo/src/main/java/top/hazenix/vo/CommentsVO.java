package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Article;
import top.hazenix.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsVO {

    private Long id;
//    private Long userId;
    //评论者名称
    private String username;
//    private Long replyId;
    //评论内容
    private String content;
    private Integer status;
    private String avatar;//评论者头像
    private Article article;//填充id和title
    private User replyPerson;//填充id和username
    //被评论者名称
    private String replyUsername;

    //评论点赞数
    private Integer likeCount;
    //评论时间
    private String createTime;
}
