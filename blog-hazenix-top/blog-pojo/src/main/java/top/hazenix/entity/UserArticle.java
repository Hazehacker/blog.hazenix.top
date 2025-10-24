package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserArticle {
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer isLiked;
    private Integer isFavorite;
}
