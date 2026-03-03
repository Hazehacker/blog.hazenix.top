package top.hazenix.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserArticle {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "文章ID", example = "1")
    private Long articleId;

    @ApiModelProperty(value = "是否点赞", example = "1")
    private Integer isLiked;

    @ApiModelProperty(value = "是否收藏", example = "1")
    private Integer isFavorite;
}
