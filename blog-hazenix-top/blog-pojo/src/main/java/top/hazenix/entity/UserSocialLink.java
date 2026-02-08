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
public class UserSocialLink {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "平台", example = "平台")
    private String platform;

    @ApiModelProperty(value = "联系方式", example = "具体联系方式")
    private String content;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime create_time;

    @ApiModelProperty(value = "更新时间", example = "2020-01-01 00:00:00")
    private LocalDateTime update_time;
}
