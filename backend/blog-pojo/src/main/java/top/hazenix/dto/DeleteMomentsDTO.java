package top.hazenix.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DeleteMomentsDTO {
    @NotEmpty(message = "删除ID列表不能为空")
    private List<Long> ids;
}
