package top.hazenix.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserDTO implements Serializable {

    private String username;
    private String email;
    private String avatar;
    private Integer gender;
//    private String password;
//    private String  phone;

    //修改密码接口用到了
    private String currentPassword;
    private String newPassword;
}
