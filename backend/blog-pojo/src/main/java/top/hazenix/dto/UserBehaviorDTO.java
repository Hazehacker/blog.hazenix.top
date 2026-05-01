package top.hazenix.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserBehaviorDTO implements Serializable {
    private Long articleId;
    private Integer duration; // 阅读时长(秒)
}
