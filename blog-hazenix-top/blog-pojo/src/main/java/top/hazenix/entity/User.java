package top.hazenix.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
//
//    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    //微信用户唯一标识
//    private String openid;

    @ApiModelProperty(value = "用户名", example = "用户名")
    private String username;

    @ApiModelProperty(value = "密码", example = "密码")
    private String password;

    @ApiModelProperty(value = "手机号", example = "手机号")
    private String phone;

    @ApiModelProperty(value = "邮箱", example = "邮箱")
    private String email;

    @ApiModelProperty(value = "刷新令牌", example = "刷新令牌")
    private String refreshToken;

    @ApiModelProperty(value = "头像", example = "头像")
    private String avatar;

    @ApiModelProperty(value = "性别[0:女 | 1:男]", example = "0")
    private Integer gender;

    @ApiModelProperty(value = "状态[0:正常 | 1:锁定]", example = "0")
    private Integer status;

    @ApiModelProperty(value = "角色[0:管理员 | 1:作者 | 2:普通用户(默认)]", example = "0")
    private Integer role;

    @ApiModelProperty(value = "注册时间", example = "2020-01-01 00:00:00")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2020-01-01 00:00:00")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "上次登录时间", example = "2020-01-01 00:00:00")
    private LocalDateTime lastLoginTime;
}
