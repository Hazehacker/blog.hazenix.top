package top.hazenix.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.hazenix.security.JsonAccessDeniedHandler;
import top.hazenix.security.JsonAuthenticationEntryPoint;
import top.hazenix.security.JwtAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离 + JWT，采用无状态会话
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 统一未登录 / 权限不足返回
                .exceptionHandling()
                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                .accessDeniedHandler(jsonAccessDeniedHandler)
                .and()
                // 配置访问控制规则
                .authorizeRequests()
                // 登录、注册 & 第三方登录相关接口放行
                .antMatchers(
                        "/user/user/login",
                        "/user/user/register",
                        "/user/user/google/**",
                        "/user/user/github/**"
                ).permitAll()
                // Swagger / Knife4j 文档放行(还有一个树洞查询接口)
                .antMatchers(
                        "/doc.html",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "user/tree/list",
                        "/api/notify/link-action"
                ).permitAll()
                // 管理端接口：需要 ADMIN 角色
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 用户端需要登录的接口（与 JwtTokenUserInterceptor 保持一致）
                .antMatchers(
                        "/user/user/logout",
                        "/user/user/userinfo",
                        "/user/user/stats",
                        "/user/user/profile",
                        "/user/user/password",
                        "/user/user/favorite",
                        "/user/tree/**",
                        "/user/articles/*/favorite"
                ).authenticated()
                // 其他接口暂时放行（后续可以按需逐步收紧）
                .anyRequest().permitAll();

        // 在用户名密码过滤器之前添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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


