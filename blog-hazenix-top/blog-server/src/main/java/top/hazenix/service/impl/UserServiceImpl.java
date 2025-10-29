package top.hazenix.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.dto.UserLoginDTO;
import top.hazenix.entity.GoogleAuthorization;
import top.hazenix.entity.User;
import top.hazenix.mapper.UserMapper;
import top.hazenix.properties.JwtProperties;
import top.hazenix.service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import top.hazenix.utils.JwtUtil;
import top.hazenix.vo.UserLoginVO;

import javax.servlet.http.HttpServletRequest;


@Service
public class UserServiceImpl implements UserService {



    @Autowired
    private GoogleAuthorization googleAuthorization;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 完成用户登录功能的相关逻辑
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        User user = userMapper.selectByEmail(userLoginDTO.getEmail());
        if (user == null) {
            throw new RuntimeException("当前邮箱还未注册");
        }
        if(!user.getPassword().equals(userLoginDTO.getPassword())){
            throw new RuntimeException("邮箱或密码错误");
        }
        user.setPassword("*");

        //更新lastLoginTime字段
        User userUse = User.builder()
                .id(user.getId())
                .lastLoginTime(LocalDateTime.now())
                .build();
        userMapper.updateLastLoginTime(userUse);

        //生成JWT，组装返回对象
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .token(token)
                .build();
        return userLoginVO;
    }

    /**
     * 完成用户注册的相关逻辑
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO register(UserLoginDTO userLoginDTO) {

        if (userMapper.selectByEmail(userLoginDTO.getEmail()) != null){
            throw new RuntimeException("当前邮箱已注册过账号");
        }
        //TODO【密码要先加密才能插入数据库】


        //插入user表
        User user = User.builder()
                .username(userLoginDTO.getUsername())
                .email(userLoginDTO.getEmail())
                .password(userLoginDTO.getPassword())
                .role(2)//默认普通用户
                .lastLoginTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        //生成JWT
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);
        //组装对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .token(token)
                .build();

        return userLoginVO;
    }

    /**
     * 完成用户退出登录的逻辑
     */
    @Override
    public void logout(HttpServletRequest request) {
        // 清除当前用户的认证信息
        // 如果使用Spring Security:
        // SecurityContextHolder.clearContext();



        // 可选：调用Google API撤销token
        // revokeGoogleToken(accessToken);
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
//        GenericUrl url = new GenericUrl("https://oauth2.googleapis.com/revoke");
//        url.put("token", accessToken);
//        HttpRequest request = requestFactory.buildGetRequest(url);
//        HttpResponse response = request.execute();
//        if (response.getStatusCode() == 200) {
//            // Token成功撤销
//            log.info("Google access token revoked successfully");
//        }

        // 获取当前JWT token
        //TODO 后面加redis了再开启
//        String token = request.getHeader(jwtProperties.getUserTokenName());
//        if(token == null){
//            throw new RuntimeException("当前用户还未登录");
//        }
//        if (StringUtils.isNotBlank(token)) {
//            // 将token加入黑名单，设置过期时间
//            String key = "jwt:blacklist:" + getTokenSignature(token);
//            redisTemplate.opsForValue().set(
//                    key,
//                    "invalid",
//                    jwtProperties.getUserTtl(),
//                    TimeUnit.MILLISECONDS
//            );
//        }



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


    /**
     * 生成用户授权URL，将用户重定向到gooogle登录页面进行身份验证
     * @return
     * @throws GeneralSecurityException
     */
    public String authorizingUrl() throws GeneralSecurityException, IOException {
        //创建HTTP传输对象和JSON工厂对象，用于处理网络请求和JSON数据解析
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        // 创建验证流程对象
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, jsonFactory, googleAuthorization.getGoogleClientSecrets(), googleAuthorization.getScopes())
                // AccessType为离线offline，才能获得Refresh Token
                .setAccessType("offline").build();
        if (googleAuthorizationCodeFlow != null) {
            // 返回跳转登录请求
            return googleAuthorizationCodeFlow.newAuthorizationUrl().setRedirectUri(googleAuthorization.getRedirectUrl()).build();
        }
        return null;
    }

    /**
     * 使用授权码获得登录token
     * @param authorizationCode
     */
    public UserLoginVO authorizingWithCode(String authorizationCode) throws GeneralSecurityException, IOException {
        //安全性验证
        if (StringUtils.isBlank(authorizationCode)) {
            throw new IllegalArgumentException("未授权");
        }
        // 限制授权码长度，防止潜在的恶意输入
        if (authorizationCode.length() > 2048) {
            throw new IllegalArgumentException("Authorization code is too long");
        }
        //TODO 这个接口开发环境的测试还没通过，配置代理麻烦
        // 创建带代理的HTTP传输对象【开发环境】   TODO 服务器环境注意调整
        HttpTransport httpTransport;
//        String proxyHost = System.getProperty("http.proxyHost");  // 例如: "127.0.0.1"
//        String proxyPort = System.getProperty("http.proxyPort");  // 例如: "1080"
        String proxyHost = "51.158.205.126";
        String proxyPort = "24957";
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS,
                    new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
            httpTransport = new NetHttpTransport.Builder()
                    .setProxy(proxy)
                    .build();
        } else {
            // 不使用代理
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        }


        // 创建请求凭证
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
// 上面代理的代码中创建了

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        //构建 Google 认证流对象
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, jsonFactory, googleAuthorization.getGoogleClientSecrets(), googleAuthorization.getScopes())
                // AccessType为离线offline，才能获得Refresh Token
                .setAccessType("offline").build();

        //构建Google 授权 请求对象
        GoogleAuthorizationCodeTokenRequest tokenRequest = googleAuthorizationCodeFlow.newTokenRequest(authorizationCode);
        tokenRequest.setRedirectUri(googleAuthorization.getRedirectUrl());
        // 发起授权请求，获得访问令牌token和刷新令牌refreshToken
        GoogleTokenResponse tokenResponse = tokenRequest.execute();
        //String accessToken = tokenResponse.getAccessToken();//如果需要用到google api，可以将他缓存
        String refreshToken = tokenResponse.getRefreshToken();


        String email = null;
        String avatar = null;
        String username = null;
        //验证并提取用户邮箱
        if (StringUtils.isNotBlank(tokenResponse.getIdToken())) {
            GoogleIdTokenVerifier idTokenVerifier = new GoogleIdTokenVerifier.Builder(googleAuthorizationCodeFlow.getTransport(), googleAuthorizationCodeFlow.getJsonFactory()).build();
            idTokenVerifier.verify(tokenResponse.getIdToken());
            GoogleIdToken googleIdToken = idTokenVerifier.verify(tokenResponse.getIdToken());
            //如果返回中有 ID Token，则进一步验证它并从中提取出注册的邮件地址等用户信息
            if (googleIdToken != null && googleIdToken.getPayload() != null) {
                email = googleIdToken.getPayload().getEmail();
                avatar = googleIdToken.getPayload().get("picture").toString();
                //获取用户名
                username = googleIdToken.getPayload().get("name").toString();
            }

        }


        User user = userMapper.selectByEmail( email);
        if(user == null){//用户没登录过，就插入，并执行主键回填
            //插入user表
            user.setUsername( username);
            user.setEmail( email);
            user.setAvatar( avatar);
            userMapper.insert(user);

        }

        user.setLastLoginTime( LocalDateTime.now());
        if (refreshToken!=null) {//【注意这里要判断不为空，因为这个字段只有第一次会有】
            //把refresh_token字段保存进去
            user.setRefreshToken( refreshToken);
        }
        userMapper.update(user);

        //生成自己的JWT
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);

        //组装返回对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(email)
                .token(token)
                .build();
        return userLoginVO;

    }




}
