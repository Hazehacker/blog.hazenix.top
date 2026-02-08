package top.hazenix.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
public class UserLoginDTO implements Serializable {

    @ApiModelProperty(value = "用户名", example = "Hazenix")
    private String username;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "密码", example = "123456user")
    private String password;
    
    @ApiModelProperty(value = "idToken（前端google登录方案用到）", example = "idToken")
    private String idToken;

}
