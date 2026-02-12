package top.hazenix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 基础的 Spring Security 配置
 *
 * 说明：
 * 1. 当前项目用户认证仍然采用自定义的 JWT 方案
 * 2. 为了不影响现有接口行为，这里暂时对所有请求放行，仅启用密码加密能力（BCrypt）
 * 3. 后续如果接入基于 Spring Security 的认证/鉴权，只需要在此类中补充规则即可
 */
@Configuration
public class SecurityConfig {

    /**
     * 对所有请求暂时放行，保留现有 JWT 拦截器逻辑
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }

    /**
     * 提供全局 PasswordEncoder Bean，使用 BCrypt 实现密码加密与校验
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


