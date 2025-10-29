package top.hazenix.query;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkQueryDTO {
    private String keyword;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
