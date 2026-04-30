package top.hazenix.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentNotifyVO {
    private Long id;
    private String username;
    private String content;
    private String articleTitle;
    private LocalDateTime createTime;
}
