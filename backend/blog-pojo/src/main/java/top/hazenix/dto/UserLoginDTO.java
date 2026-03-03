package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserLoginDTO implements Serializable {

    @ApiModelProperty(value = "用户名", example = "Hazenix")
    @Size(max = 30, message = "用户名长度不能超过30个字符")
    private String username;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @ApiModelProperty(value = "密码", example = "123456user")
    @Size(min = 3, max = 20, message = "密码长度必须在3-20个字符之间")
    private String password;
    
    @ApiModelProperty(value = "idToken（前端google登录方案用到）", example = "idToken")
    private String idToken;

}
