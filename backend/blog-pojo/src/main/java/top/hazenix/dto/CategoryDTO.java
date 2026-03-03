package top.hazenix.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @ApiModelProperty(value = "分类名称", example = "技术")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    @ApiModelProperty(value = "分类状态[0:正常 | 1:未启用]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
    
    @ApiModelProperty(value = "URL标识符", example = "category-slug")
    @Size(max = 100, message = "URL标识符长度不能超过100个字符")
    private String slug;
}
