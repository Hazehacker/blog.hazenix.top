package top.hazenix.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SiteLike {
    private Long id;
    private String ipHash;
    private LocalDateTime createdAt;
}