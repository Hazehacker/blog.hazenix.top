package top.hazenix.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章ID", example = "1")
    private Long articleId;

    @ApiModelProperty(value = "父级评论ID", example = "1")
    private Long parentId;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "用户名", example = "用户名")
    private String username;

    @ApiModelProperty(value = "回复用户ID", example = "1")
    private Long replyId;

    @ApiModelProperty(value = "回复用户名", example = "用户名")
    private String replyUsername;

    @ApiModelProperty(value = "内容", example = "内容")
    private String content;

    @ApiModelProperty(value = "状态[0:正常 | 1:不展示]", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

}
