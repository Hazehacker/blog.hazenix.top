package top.hazenix.dto;

import lombok.Data;

/**
 * 新增树洞弹幕传输类
 */
@Data
public class TreeCommentsDTO {
    private Long userId;
    private String username;
    private String content;
}
