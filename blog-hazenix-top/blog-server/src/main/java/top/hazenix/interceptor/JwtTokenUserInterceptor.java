package top.hazenix.interceptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.properties.JwtProperties;
import top.hazenix.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("JWT User Interceptor 执行: URI = {}", request.getRequestURI());
        //TODO 后面删掉
        if(1 ==1 ){
            //暂时不开启jwt，直接放行
            return true;
        }
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());
        //TODO 后面加redis了再开启
        //在JWT验证时增加黑名单检查
//        if(isTokenInBlacklist( token)){
//            return false;
//        }
        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            log.info("1");
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
            log.info("当前用户id：{}", userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
    // 在JWT验证时增加黑名单检查
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
