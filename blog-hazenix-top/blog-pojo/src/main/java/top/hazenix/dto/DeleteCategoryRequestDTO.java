package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCategoryRequestDTO {
    @ApiModelProperty(value = "ID列表", example = "[1, 2, 3]")
    @NotEmpty(message = "ID列表不能为空")
    private List<Integer> ids;
}
