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
public class UserSocialLink {
    private Long id;
    private Long userId;
    //平台
    private String platform;
    //具体联系方式
    private String content;

    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
