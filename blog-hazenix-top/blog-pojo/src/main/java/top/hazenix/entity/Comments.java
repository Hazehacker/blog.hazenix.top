package top.hazenix.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    private Long id;
    private Long articleId;
    private Long userId;
    private String username;
    private Long replyId;
    private String replyUsername;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;

}
