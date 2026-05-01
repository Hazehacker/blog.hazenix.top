package top.hazenix.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class UserInterestDTO implements Serializable {
    private List<Long> tagIds;
}
