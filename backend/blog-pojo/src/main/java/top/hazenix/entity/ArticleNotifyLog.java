package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleNotifyLog {
    private Long id;
    private Long articleId;
    private LocalDateTime sendTime;
    private Integer successCount;
    private Integer failCount;
}