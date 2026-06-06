package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 新增树洞弹幕传输类
 */
@Data
public class TreeCommentsDTO {
    @ApiModelProperty(value = "用户ID（匿名时可为空）", example = "1")
    private Long userId;

    @ApiModelProperty(value = "用户名", example = "username")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    private String username;

    @ApiModelProperty(value = "内容", example = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容长度不能超过500个字符")
    private String content;

    @ApiModelProperty(value = "匿名用户邮箱（选填）", example = "guest@example.com")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @ApiModelProperty(value = "是否匿名", example = "false")
    private Boolean isAnonymous;
}
