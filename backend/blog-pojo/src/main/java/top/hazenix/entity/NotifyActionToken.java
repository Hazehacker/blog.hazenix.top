package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotifyActionToken {
    private Long id;
    private String token;
    private String targetType;
    private Long targetId;
    private String action;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createTime;
}
