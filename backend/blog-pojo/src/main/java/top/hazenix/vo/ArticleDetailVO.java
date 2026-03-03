package top.hazenix.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Article;
import top.hazenix.entity.Tags;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVO extends Article {

    @ApiModelProperty(value = "分类名称", example = "技术")
    private String categoryName;
    
    @ApiModelProperty(value = "文章标签集合（包含id和name）")
    private List<Tags> tags = new ArrayList<>();//填充id和name
    
    @ApiModelProperty(value = "评论数", example = "10")
    private Integer commentCount;
    
    @ApiModelProperty(value = "当前用户是否点赞过[0:未点赞 | 1:已点赞]", example = "0")
    private Integer isLiked;
    
    @ApiModelProperty(value = "当前用户是否收藏过[0:未收藏 | 1:已收藏]", example = "0")
    private Integer isFavorite;

}
