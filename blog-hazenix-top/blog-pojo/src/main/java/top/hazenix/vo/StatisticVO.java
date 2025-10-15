package top.hazenix.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticVO {
    //文章总数
    private Integer totalArticles;
    //文章种类数
    private Integer totalCategories;
    //文章标签数
    private Integer totalTags;
    //评论总数
    private Integer totalComments;
}
