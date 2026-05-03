package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ArticleSubscription {
    private Long id;
    private String email;
    private String unsubscribeToken;
    private Integer status;  // 1=已激活 2=已退订
    private LocalDateTime subscribeAt;
    private LocalDateTime unsubscribeAt;
}