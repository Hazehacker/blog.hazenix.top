package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Article;

import java.util.ArrayList;
import java.util.List;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class ArticleDetailVO extends Article {

    //文章标签集合
    private List<Integer> tagsId = new ArrayList<>();

}
