package top.hazenix.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticVO {
    @ApiModelProperty(value = "文章总数", example = "100")
    private Integer totalArticles;
    
    @ApiModelProperty(value = "文章种类数", example = "10")
    private Integer totalCategories;
    
    @ApiModelProperty(value = "文章标签数", example = "20")
    private Integer totalTags;
    
    @ApiModelProperty(value = "评论总数", example = "500")
    private Integer totalComments;
}
