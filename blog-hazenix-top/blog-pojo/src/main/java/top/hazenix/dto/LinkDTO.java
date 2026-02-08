package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {
    @ApiModelProperty(value = "名称", example = "博客名称")
    private String name;

    @ApiModelProperty(value = "描述", example = "博客描述")
    private String description;

    @ApiModelProperty(value = "链接", example = "https://example.com")
    private String url;

    @ApiModelProperty(value = "头像", example = "avatar_url")
    private String avatar;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
}
