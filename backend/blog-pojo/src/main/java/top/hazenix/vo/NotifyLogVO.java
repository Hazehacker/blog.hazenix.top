package top.hazenix.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class NotifyLogVO {
    private Long id;
    private LocalDate statDate;
    private LocalDateTime sendTime;
    private Integer status;
    private Integer retryCount;
    private String errorMsg;
    private String recipient;
}
