package top.hazenix.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;
//    private String openid;
    
    @ApiModelProperty(value = "用户名", example = "username")
    private String username;
    
    @ApiModelProperty(value = "头像", example = "avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "令牌", example = "token")
    private String token;

}
