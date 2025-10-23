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
public class Tags {

    private Long id;

    //标签名
    private String name;

    //URL标识符
    private String slug;

    //排序字段
    private Integer sort;
    //状态：0禁用，1启用
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;


}
