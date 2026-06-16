package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSlugVO {
    private Long id;
    private String slug;
    private LocalDateTime updateTime;
}
