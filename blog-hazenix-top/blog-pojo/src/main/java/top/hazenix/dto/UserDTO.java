package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserDTO implements Serializable {

    @ApiModelProperty(value = "用户名", example = "username")
    private String username;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "头像", example = "avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "性别[0:女 | 1:男]", example = "0")
    private Integer gender;
//    private String password;
//    private String  phone;

    @ApiModelProperty(value = "当前密码", example = "currentPassword")
    private String currentPassword;
    
    @ApiModelProperty(value = "新密码", example = "newPassword")
    private String newPassword;
}
