package top.hazenix.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "标题", example = "标题")
    private String title;

//    private String summary;//这个字段待定 TODO
    @ApiModelProperty(value = "内容", example = "内容")
    private String content;

    @ApiModelProperty(value = "封面图片", example = "封面图片")
    private String coverImage;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Integer categoryId;

    @ApiModelProperty(value = "点赞数", example = "100")
    private Integer likeCount;

    @ApiModelProperty(value = "收藏数", example = "100")
    private Integer favoriteCount;

    @ApiModelProperty(value = "浏览数", example = "1000")
    private Integer viewCount;

    @ApiModelProperty(value = "URL标识符", example = "url-identifier")
    private String slug;

    //TODO "metaDescription": "SEO描述", 后期看下加不加
    @ApiModelProperty(value = "是否置顶[0:不置顶 | 1:置顶]", example = "1")
    private Integer isTop;

    @ApiModelProperty(value = "状态[0:正常 | 1:待审核 | 2:草稿]", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2020-01-01 00:00:00")
    private LocalDateTime updateTime;
}
