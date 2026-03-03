package top.hazenix.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tags implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "标签名称", example = "标签名称")
    private String name;

    @ApiModelProperty(value = "标签URL标识符", example = "标签URL标识符")
    private String slug;

    @ApiModelProperty(value = "排序字段", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "状态[0:禁用 | 1:启用]", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间", example = "2020-01-01 00:00:00")
    private LocalDateTime updateTime;


}
