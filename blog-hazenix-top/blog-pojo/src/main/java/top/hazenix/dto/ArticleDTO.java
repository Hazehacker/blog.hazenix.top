package top.hazenix.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ArticleDTO {
    @ApiModelProperty(value = "标题", example = "文章标题")
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;
//    private String summary;
    
    @ApiModelProperty(value = "内容", example = "文章内容")
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    @ApiModelProperty(value = "是否置顶[0:不置顶 | 1:置顶]", example = "0")
    private Integer isTop;//0不置顶，1置顶
    
    @ApiModelProperty(value = "状态[0:正常 | 1:待审核 | 2:草稿]", example = "0")
    private Integer status;
    
    @ApiModelProperty(value = "分类ID", example = "1")
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;
    
    @ApiModelProperty(value = "标签ID列表", example = "[1, 2, 3]")
    private List<Integer> tagIds;
    
    @ApiModelProperty(value = "封面图片", example = "cover_image_url")
    private String coverImage;
    
    @ApiModelProperty(value = "URL标识符", example = "article-slug")
    @Size(max = 100, message = "URL标识符长度不能超过100个字符")
    private String slug;
}
