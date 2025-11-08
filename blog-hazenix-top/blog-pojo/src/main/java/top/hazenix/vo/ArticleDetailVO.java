package top.hazenix.vo;

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

    //文章标签集合
    private String categoryName;
    private List<Tags> tags = new ArrayList<>();//填充id和name
    private Integer commentCount;
    //当前用户数是否点赞过
    private Integer isLiked;
    //当前用户数是否收藏过
    private Integer isFavorite;

}
