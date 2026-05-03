package top.hazenix.entity;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleUrge {
    private Long id;
    private String urgeMonth;  // YYYY-MM
    private Integer count;
}