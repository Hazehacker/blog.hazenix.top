package top.hazenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkDTO {
    private String name;
    private String description;
    private String url;
    private String avatar;
    private Integer sort;
    private Integer status;
}
