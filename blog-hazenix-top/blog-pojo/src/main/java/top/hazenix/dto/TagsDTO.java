package top.hazenix.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TagsDTO {
    @ApiModelProperty(value = "标签名称", example = "标签名称")
    private String name;
    
    @ApiModelProperty(value = "标签URL标识符", example = "tag-slug")
    private String slug;
    
    @ApiModelProperty(value = "排序字段", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "状态[0:禁用 | 1:启用]", example = "1")
    private Integer status;
}
