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
public class Link {
    private Long id;
    private String name;
    private String description;
    private String url;
    private String avatar;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
