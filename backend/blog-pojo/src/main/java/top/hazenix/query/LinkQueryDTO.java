package top.hazenix.query;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkQueryDTO {
    @ApiModelProperty(value = "关键词", example = "关键词")
    private String keyword;
    
    @ApiModelProperty(value = "状态[0:正常 | 1:待审核]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "页码", example = "1")
    @Builder.Default
    private Integer page = 1;
    
    @ApiModelProperty(value = "每页显示记录数", example = "20")
    @Builder.Default
    private Integer pageSize = 20;
}
