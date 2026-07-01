package top.hazenix.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomentVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "正文")
    private String content;

    @ApiModelProperty(value = "图片URL列表（已解析）")
    private List<String> images;

    @ApiModelProperty(value = "图片JSON字符串（Mapper 直接映射用，Service 层反序列化后填充 images）")
    private String imagesJson;

    @ApiModelProperty(value = "标签列表")
    private List<String> tags;

    @ApiModelProperty(value = "点赞数")
    private Integer likeCount;

    @ApiModelProperty(value = "浏览数")
    private Integer viewCount;

    @ApiModelProperty(value = "状态[0:正常 | 1:草稿]")
    private Integer status;

    @ApiModelProperty(value = "当前 IP 是否已点赞")
    private Boolean liked;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "推荐度[0:屏蔽 | 1:弱 | 2:较弱 | 3:默认 | 4:推荐 | 5:精华]")
    private Integer recommendLevel;

    @ApiModelProperty(value = "是否置顶[0:否 | 1:是]")
    private Integer isTop;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
