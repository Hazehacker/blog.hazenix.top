package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotifyLog {
    private Long id;
    private LocalDate statDate;
    private LocalDateTime sendTime;
    private Integer status;
    private Integer retryCount;
    private String errorMsg;
    private String recipient;
}
