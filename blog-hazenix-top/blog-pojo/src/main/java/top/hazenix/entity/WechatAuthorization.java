package top.hazenix.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WechatAuthorization {
    private String appId;
    private String appSecret;
    private String redirectUrl;
    private String scope;
}


