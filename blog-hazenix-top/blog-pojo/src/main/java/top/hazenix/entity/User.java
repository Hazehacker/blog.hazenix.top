package top.hazenix.entity;

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

    private Long id;

    //微信用户唯一标识
//    private String openid;

    //姓名
    private String username;

    private String password;

    //手机号
    private String phone;

    //邮箱
    private String email;

    //refreshToken
    private String refreshToken;

    //头像
    private String avatar;

    //性别 0 女 1 男
    private Integer gender;

    //身份证号
    private Integer status;

    //角色
    private Integer role;

    //注册时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;

    //上次登录时间
    private LocalDateTime lastLoginTime;
}
