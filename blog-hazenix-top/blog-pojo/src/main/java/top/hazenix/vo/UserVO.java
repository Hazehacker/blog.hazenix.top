package top.hazenix.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private Long id;
//    private String openid;
    //用户名
    private String username;
    //头像
    private String avatar;
    //性别
    private Integer gender;
    //邮箱
    private String email;


}
