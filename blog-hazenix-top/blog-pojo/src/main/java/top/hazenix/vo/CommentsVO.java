package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Article;
import top.hazenix.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    //父级评论id
    private Long parentId;
    //评论者id
    private Long userId;
    //评论者名称
    private String username;
    //评论者头像
    private String avatar;
    //评论内容
    private String content;
    //文章id
    private Long articleId;
    //文章标题
    private String articleTitle;
    //被回复者id
    //(TODO 评论者或被回复者如果改了用户名，要在comements表里面更新username或reply_username字段)
    private Long replyId;
    //被回复者名称
    private String replyUsername;
    //评论状态
    private Integer status;
    //评论时间
    private LocalDateTime createTime;
    //子评论列表
    private List<CommentsVO> replies = new ArrayList<>();

}
