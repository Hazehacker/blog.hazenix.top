package top.hazenix.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    private Long id;
    private Long userId;
    private String title;
//    private String summary;//这个字段待定 TODO
    private String content;
    private String coverImage;
    private Integer categoryId;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer viewCount;
    //URL标识符
    private String slug;
    //TODO "metaDescription": "SEO描述", 后期看下加不加
    private Integer isTop;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
