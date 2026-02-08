package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCategoryRequestDTO {
    @ApiModelProperty(value = "ID列表", example = "[1, 2, 3]")
    private List<Integer> ids;
}
