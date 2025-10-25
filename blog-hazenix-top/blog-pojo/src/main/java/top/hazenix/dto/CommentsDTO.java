package top.hazenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsDTO {
    private Long articleId;
    private Integer status;

    //评论内容
    private String content;
    //评论者名称
    private String username;
    //被评论者
    private Long replyId;
}
