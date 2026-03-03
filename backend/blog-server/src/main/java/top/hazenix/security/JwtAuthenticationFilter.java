package top.hazenix.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.constant.UserConstants;
import top.hazenix.context.BaseContext;
import top.hazenix.entity.User;
import top.hazenix.mapper.UserMapper;
import top.hazenix.properties.JwtProperties;
import top.hazenix.utils.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于JWT的SpringSecutity认证过滤器
 *
 * 作用：
 * 1. 从请求头中解析出 JWT
 * 2. 校验是否在黑名单中
 * 3. 解析出用户信息，构建 Spring Security 的 Authentication
 * 4. 同时维护项目原有的 BaseContext，保证旧代码可以继续通过 BaseContext 获取 userId
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final RedisTemplate redisTemplate;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String tokenHeaderName = jwtProperties.getUserTokenName();
        String token = request.getHeader(tokenHeaderName);

        try {
            if (StringUtils.isNotBlank(token) && !isTokenInBlacklist(token)) {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

                // 查询用户信息（用于构建权限）
                User user = userMapper.getById(userId);
                if (user != null && (user.getStatus() == null || !user.getStatus().equals(UserConstants.STATUS_LOCKED))) {
                    List<GrantedAuthority> authorities = buildAuthorities(user);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // 兼容旧代码：继续使用 BaseContext 传递 userId
                    BaseContext.setCurrentId(userId);
                }
            }
        } catch (Exception ex) {
            log.warn("JWT 解析失败, URI = {}, error = {}", requestURI, ex.getMessage());
            SecurityContextHolder.clearContext();
            BaseContext.removeCurrentId();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 避免线程复用导致的脏数据
            BaseContext.removeCurrentId();
        }
    }

    /**
     * 根据用户角色构建权限集合
     */
    private List<GrantedAuthority> buildAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            if (UserConstants.ROLE_ADMIN.equals(user.getRole())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (UserConstants.ROLE_AUTHOR.equals(user.getRole())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_AUTHOR"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        }
        return authorities;
    }

    /**
     * 在 JWT 验证时增加黑名单检查
     */
    private boolean isTokenInBlacklist(String token) {
        String key = "jwt:blacklist:" + getTokenSignature(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String getTokenSignature(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String[] chunks = token.split("\\.");
        if (chunks.length > 2) {
            return chunks[2]; // signature part
        }
        return null;
    }
}

