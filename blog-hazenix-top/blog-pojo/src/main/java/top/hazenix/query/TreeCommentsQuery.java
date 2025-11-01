package top.hazenix.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeCommentsQuery {
    @Builder.Default
    private Integer page = 1;
    @Builder.Default
    private Integer pageSize = 20;
    private String keyword;
    private Integer status;
    private String username;
}
