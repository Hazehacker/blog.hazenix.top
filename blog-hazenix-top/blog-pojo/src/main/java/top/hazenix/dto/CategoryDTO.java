package top.hazenix.dto;


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
public class CategoryDTO {
    @ApiModelProperty(value = "分类名称", example = "技术")
    private String name;
    
    @ApiModelProperty(value = "分类状态[0:正常 | 1:未启用]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "URL标识符", example = "category-slug")
    private String slug;
}
