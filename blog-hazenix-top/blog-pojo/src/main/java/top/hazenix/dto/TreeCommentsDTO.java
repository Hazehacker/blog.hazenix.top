package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新增树洞弹幕传输类
 */
@Data
public class TreeCommentsDTO {
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;
    
    @ApiModelProperty(value = "用户名", example = "username")
    private String username;
    
    @ApiModelProperty(value = "内容", example = "评论内容")
    private String content;
}
