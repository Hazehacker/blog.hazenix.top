package top.hazenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTagsDTO {
    private Long articleId;
    private Integer tagsId;
    private String tagsName;
}
