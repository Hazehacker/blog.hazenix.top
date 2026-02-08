package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {
    @ApiModelProperty(value = "名称", example = "博客名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String name;
    
    @ApiModelProperty(value = "描述", example = "博客描述")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
    
    @ApiModelProperty(value = "链接", example = "https://example.com")
    @NotBlank(message = "链接不能为空")
    @URL(message = "链接格式不正确")
    private String url;
    
    @ApiModelProperty(value = "头像", example = "avatar_url")
    @URL(message = "头像链接格式不正确")
    private String avatar;
    
    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
}
