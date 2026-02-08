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
public class ArticleListQuery {
    @ApiModelProperty(value = "标题", example = "文章标题")
    private String title;
    
    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer categoryId;
    
    @ApiModelProperty(value = "标签ID", example = "1")
    private Long tagId;
    
    @ApiModelProperty(value = "状态[0:正常 | 1:待审核 | 2:草稿]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;
}
