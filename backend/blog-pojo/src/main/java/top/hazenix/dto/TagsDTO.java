package top.hazenix.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class TagsDTO {
    @ApiModelProperty(value = "标签名称", example = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;
    
    @ApiModelProperty(value = "标签URL标识符", example = "tag-slug")
    @Size(max = 100, message = "标签URL标识符长度不能超过100个字符")
    private String slug;
    
    @ApiModelProperty(value = "排序字段", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "状态[0:禁用 | 1:启用]", example = "1")
    private Integer status;
}
