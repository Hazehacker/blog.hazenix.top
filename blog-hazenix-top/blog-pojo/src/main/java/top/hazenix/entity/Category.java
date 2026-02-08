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
public class Category {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "分类名称", example = "技术")
    private String name;

    @ApiModelProperty(value = "分类状态[0:正常 | 1:未启用]", example = "1")
    private Integer status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "URL标识符", example = "url-identifier")
    private String slug;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2020-01-01 00:00:00")
    private LocalDateTime updateTime;
}
