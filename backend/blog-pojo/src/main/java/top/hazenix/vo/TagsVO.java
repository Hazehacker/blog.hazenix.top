package top.hazenix.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "标签名称", example = "标签名称")
    private String name;
    
    @ApiModelProperty(value = "文章数量", example = "10")
    private Integer articleCount;
}
