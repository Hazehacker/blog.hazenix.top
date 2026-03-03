package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTagsDTO {
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long articleId;
    
    @ApiModelProperty(value = "标签ID", example = "1")
    private Integer tagsId;
    
    @ApiModelProperty(value = "标签名称", example = "标签名称")
    private String tagsName;
}
