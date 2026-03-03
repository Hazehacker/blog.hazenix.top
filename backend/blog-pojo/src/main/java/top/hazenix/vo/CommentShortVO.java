package top.hazenix.vo;


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
public class CommentShortVO {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "评论者", example = "username")
    private String username;
    
    @ApiModelProperty(value = "评论内容", example = "评论内容")
    private String content;
    
    @ApiModelProperty(value = "评论时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

}
