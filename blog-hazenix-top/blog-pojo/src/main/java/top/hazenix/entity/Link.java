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
public class Link {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "名称", example = "名称")
    private String name;

    @ApiModelProperty(value = "描述", example = "描述")
    private String description;

    @ApiModelProperty(value = "链接", example = "链接")
    private String url;

    @ApiModelProperty(value = "头像", example = "头像")
    private String avatar;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "状态[0:正常 | 1:待审核]", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;
}
