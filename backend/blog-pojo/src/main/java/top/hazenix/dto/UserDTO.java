package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserDTO implements Serializable {

    @ApiModelProperty(value = "用户名", example = "username")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    private String username;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @ApiModelProperty(value = "头像", example = "avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "性别[0:女 | 1:男]", example = "0")
    private Integer gender;
//    private String password;
//    private String  phone;

    @ApiModelProperty(value = "当前密码", example = "currentPassword")
    @Size(min = 3, max = 20, message = "当前密码长度必须在3-20个字符之间")
    private String currentPassword;
    
    @ApiModelProperty(value = "新密码", example = "newPassword")
    @Size(min = 3, max = 20, message = "新密码长度必须在3-20个字符之间")
    private String newPassword;
}
