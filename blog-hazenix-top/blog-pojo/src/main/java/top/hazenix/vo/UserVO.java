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
public class UserVO implements Serializable {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;
//    private String openid;
    
    @ApiModelProperty(value = "用户名", example = "username")
    private String username;
    
    @ApiModelProperty(value = "头像", example = "avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "性别[0:女 | 1:男]", example = "0")
    private Integer gender;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;


}
