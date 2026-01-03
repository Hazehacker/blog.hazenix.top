package top.hazenix.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hazenix.entity.WechatAuthorization;
import top.hazenix.properties.WeChatProperties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WechatAuthorizationConfig {

    private final WeChatProperties weChatProperties;

    @Bean(name = "wechatAuthorization")
    public WechatAuthorization wechatAuthorization() {
        // 构建bean
        return WechatAuthorization.builder()
                .appId(weChatProperties.getAppid())
                .appSecret(weChatProperties.getSecret())
                .redirectUrl(weChatProperties.getRedirectUrl())
                .scope(weChatProperties.getScope())
                .build();
    }
}

