package top.hazenix.dto;


import lombok.Data;

import java.util.List;

@Data
public class TagsDTO {
    private String name;
    private String slug;
    private Integer sort;
    private Integer status;
}
