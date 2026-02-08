package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsDTO {
    @ApiModelProperty(value = "文章ID", example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @ApiModelProperty(value = "父级评论ID", example = "1")
    private Long parentId;
    
    @ApiModelProperty(value = "状态[0:正常 | 1:不展示]", example = "0")
    private Integer status;

    @ApiModelProperty(value = "评论内容", example = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    private String content;
    
    @ApiModelProperty(value = "评论者名称", example = "username")
    @Size(max = 30, message = "评论者名称长度不能超过30个字符")
    private String username;
    
    @ApiModelProperty(value = "被评论者ID", example = "1")
    private Long replyId;
}
