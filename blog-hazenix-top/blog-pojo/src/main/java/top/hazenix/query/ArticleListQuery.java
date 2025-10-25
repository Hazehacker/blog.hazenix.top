package top.hazenix.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleListQuery {
    private String title;
    private Integer categoryId;
    private Long tagId;
    private Integer status;
    private Long userId;
}
