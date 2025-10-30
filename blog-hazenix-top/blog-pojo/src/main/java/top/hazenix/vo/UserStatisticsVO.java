package top.hazenix.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsVO {
    private Integer favoriteCount;
    private Integer likeCount;
    private Integer commentsCount;
}
