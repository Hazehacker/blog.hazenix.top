package top.hazenix.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.hazenix.entity.Category;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleShortVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "标题", example = "文章标题")
    private String title;
    
    @ApiModelProperty(value = "状态[0:正常 | 2:草稿]", example = "0")
    private Integer status;//0正常  2草稿
    
    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

    //private String summary;//后面考虑添加

    @ApiModelProperty(value = "分类信息（包含id和name两个属性）")
    private Category category;//自己联表查询填充(包含id和name两个属性)
    
    @ApiModelProperty(value = "是否置顶[0:不置顶 | 1:置顶]", example = "0")
    private Integer isTop;
    
    @ApiModelProperty(value = "点赞数", example = "100")
    private Integer likeCount;
    
    @ApiModelProperty(value = "收藏数", example = "100")
    private Integer favoriteCount;
    
    @ApiModelProperty(value = "浏览数", example = "1000")
    private Integer viewCount;




}
