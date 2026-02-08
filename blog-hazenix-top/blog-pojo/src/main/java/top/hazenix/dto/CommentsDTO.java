package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsDTO {
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long articleId;
    
    @ApiModelProperty(value = "父级评论ID", example = "1")
    private Long parentId;
    
    @ApiModelProperty(value = "状态[0:正常 | 1:不展示]", example = "0")
    private Integer status;

    @ApiModelProperty(value = "评论内容", example = "评论内容")
    private String content;
    
    @ApiModelProperty(value = "评论者名称", example = "username")
    private String username;
    
    @ApiModelProperty(value = "被评论者ID", example = "1")
    private Long replyId;
}
