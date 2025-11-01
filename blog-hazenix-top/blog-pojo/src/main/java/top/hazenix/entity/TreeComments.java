package top.hazenix.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeComments {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer status;
    private LocalDateTime createTime;


}
