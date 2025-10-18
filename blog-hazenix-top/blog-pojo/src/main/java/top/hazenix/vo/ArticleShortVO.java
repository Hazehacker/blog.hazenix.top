package top.hazenix.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Category;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleShortVO {
    private Long id;
    private String title;
    private Integer status;//0正常  2草稿
    private LocalDateTime createTime;

    //private String summary;//后面考虑添加

    private Category category;//自己联表查询填充(包含id和name两个属性)
    private Integer isTop;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer viewCount;




}
