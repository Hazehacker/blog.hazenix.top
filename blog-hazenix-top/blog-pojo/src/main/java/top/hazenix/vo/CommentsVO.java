package top.hazenix.vo;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "父级评论ID", example = "1")
    private Long parentId;
    
    @ApiModelProperty(value = "评论者ID", example = "1")
    private Long userId;
    
    @ApiModelProperty(value = "评论者名称", example = "username")
    private String username;
    
    @ApiModelProperty(value = "评论者头像", example = "avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "评论内容", example = "评论内容")
    private String content;
    
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long articleId;
    
    @ApiModelProperty(value = "文章标题", example = "文章标题")
    private String articleTitle;
    
    @ApiModelProperty(value = "被回复者ID", example = "1")
    private Long replyId;
    
    @ApiModelProperty(value = "被回复者名称", example = "replyUsername")
    private String replyUsername;
    
    @ApiModelProperty(value = "评论状态[0:正常 | 1:不展示]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "评论时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "子评论列表")
    private List<CommentsVO> replies = new ArrayList<>();

}
