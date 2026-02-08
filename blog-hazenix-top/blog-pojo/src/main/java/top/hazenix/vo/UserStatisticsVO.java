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
public class UserStatisticsVO {
    @ApiModelProperty(value = "收藏数", example = "10")
    private Integer favoriteCount;
    
    @ApiModelProperty(value = "点赞数", example = "20")
    private Integer likeCount;
    
    @ApiModelProperty(value = "评论数", example = "5")
    private Integer commentsCount;
}
