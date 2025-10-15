package top.hazenix.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
