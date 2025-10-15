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
public class Article {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String coverImage;
    private Long tagsId;
    private String tagsName;
    private Integer categoryId;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer viewCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
