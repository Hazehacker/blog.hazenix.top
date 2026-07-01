package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MomentDTO {

    @ApiModelProperty(value = "标题（可选）")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @ApiModelProperty(value = "正文")
    @NotBlank(message = "正文不能为空")
    private String content;

    @ApiModelProperty(value = "图片URL列表（最多9张）")
    private List<String> imageUrls;

    @ApiModelProperty(value = "标签ID列表")
    private List<Integer> tagIds;

    @ApiModelProperty(value = "状态[0:正常 | 1:草稿]")
    private Integer status;
}
