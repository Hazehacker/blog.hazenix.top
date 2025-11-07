package top.hazenix.dto;


import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {
    private String title;
//    private String summary;
    private String content;
    private Integer isTop;//0不置顶，1置顶
    private Integer status;
    private Integer categoryId;
    private List<Integer> tagIds;
    private String coverImage;
    private String slug;
}
