package top.hazenix.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import top.hazenix.constant.ErrorCode;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.constant.MessageConstant;
import top.hazenix.constant.UserConstants;
import top.hazenix.exception.BussinessException;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserDTO;
import top.hazenix.dto.UserLoginDTO;
import top.hazenix.entity.GithubAuthorization;
import top.hazenix.entity.GoogleAuthorization;
import top.hazenix.entity.User;
import top.hazenix.mapper.CommentsMapper;
import top.hazenix.mapper.UserArticleMapper;
import top.hazenix.mapper.UserMapper;
import top.hazenix.properties.JwtProperties;
import top.hazenix.service.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import top.hazenix.utils.JwtUtil;
import top.hazenix.vo.UserLoginVO;
import top.hazenix.vo.UserStatisticsVO;
import top.hazenix.vo.UserVO;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {




    private final GoogleAuthorization googleAuthorization;
    private final GithubAuthorization githubAuthorization;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final RedisTemplate redisTemplate;
    private final UserArticleMapper userArticleMapper;
    private final CommentsMapper commentsMapper;
    private final PasswordEncoder passwordEncoder;

    private final String GOOGLE_ISSUER = "https://accounts.google.com";

    /**
     * 完成用户登录功能的相关逻辑
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        User user = userMapper.selectByEmail(userLoginDTO.getEmail());
        if (user == null) {
            throw new BussinessException(ErrorCode.A01001, MessageConstant.CURRENT_EMAIL_NOT_REGISTERD);
        }
        //【数据库的密码是使用 BCrypt 加密的】
        if(!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())){
            throw new BussinessException(ErrorCode.A01002, MessageConstant.EMAIL_OR_PASSWORD_ERROR);
        }
        if(user.getStatus()!=null && user.getStatus()==1){
            throw new BussinessException(ErrorCode.A01005, MessageConstant.CURRENT_USER_IS_ILLEGAL);
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
            throw new BussinessException(ErrorCode.A01004, MessageConstant.CURRENT_EMAIL_HAS_REGISTERED);
        }
        //插入user表
        User user = User.builder()
                .username(userLoginDTO.getUsername())
                .email(userLoginDTO.getEmail())
                //【使用 BCrypt 对密码进行加密存储】
                .password(passwordEncoder.encode(userLoginDTO.getPassword()))
                .role(UserConstants.ROLE_USER)//默认普通用户
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

        String token = request.getHeader(jwtProperties.getUserTokenName());
        if(token == null){
            throw new BussinessException(ErrorCode.A01003, MessageConstant.USER_NOT_LOGIN);
        }
        if (StringUtils.isNotBlank(token)) {
            // 将token加入黑名单，设置过期时间
            String key = "jwt:blacklist:" + getTokenSignature(token);
            redisTemplate.opsForValue().set(
                    key,
                    "invalid",
                    jwtProperties.getUserTtl(),
                    TimeUnit.MILLISECONDS
            );
        }



    }

    /**
     * 获取当前用户信息
     * @return
     */
    @Override
    public UserVO getUserInfo() {
        Long  userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BussinessException(ErrorCode.A01003, MessageConstant.USER_NOT_LOGIN);
        }
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .email(user.getEmail())
                .build();
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
     * 【前端方案】使用google idToken登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO idTokenLogin(UserLoginDTO userLoginDTO) throws ParseException {
        String idToken = userLoginDTO.getIdToken();
        if(StringUtils.isBlank(idToken)){
            throw new BussinessException(ErrorCode.A01006, MessageConstant.ID_TOKEN_IS_EMPTY);
        }

        //验证并提取用户邮箱

        //前端方案的目的就是规避网络问题，这里使用 Google 公布的公钥（JWKS）在本地验证签名，而不使用google的api验证id_token

        JWTClaimsSet claims = verifyAndParseIdToken(idToken);
        if (claims == null ) {
            throw new SecurityException(MessageConstant.GOOGLE_LOGIN_ERROR);
        }
        String email = claims.getStringClaim("email");
        String username = claims.getStringClaim("name");
        String avatar = claims.getStringClaim("picture");



        UserLoginVO userLoginVO = loginLogic(email,username,avatar,null);

        return userLoginVO;
    }

    private JWTClaimsSet verifyAndParseIdToken(String idToken){
        try {
            // 1. 从配置中获取 JWKS JSON 字符串（例如："{\"keys\":[...]}"）
            String jwksJson = googleAuthorization.getJwks(); // 确保这是完整的 JSON 字符串

            // 2. 解析为 JWKSet 对象
            JWKSet jwkSet = JWKSet.parse(jwksJson);

            // 3. 创建无网络依赖的 JWKSource
            JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

            // 4. 配置 JWT 处理器
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            jwtProcessor.setJWTClaimsSetVerifier((claimsSet, context) -> {
                // 验证 issuer
                if (!GOOGLE_ISSUER.equals(claimsSet.getIssuer())) {
                    throw new BussinessException(ErrorCode.B01002, "无效的issuer: " + claimsSet.getIssuer());
                }
                // 验证 audience（必须包含你的 client_id）
                Object aud = claimsSet.getAudience();
                if (aud == null || !aud.toString().contains(googleAuthorization.getClientId())) {
                    throw new BussinessException(ErrorCode.A01007, MessageConstant.AUDIENCE_INVALID);
                }
                // 验证时间（exp, nbf）
                // 3. 验证时间有效性
                Date now = new Date();
                Date exp = claimsSet.getExpirationTime();
                Date nbf = claimsSet.getNotBeforeTime();
                if (exp != null && exp.before(now)) {
                    throw new SecurityException(MessageConstant.ID_TOKEN_IS_EXPIRED);
                }
                if (nbf != null && nbf.after(now)) {
                    throw new SecurityException(MessageConstant.ID_TOKEN_INVALID);
                }
            });


            // 处理并验证 token
            SecurityContext ctx = null; // 可为 null
            return jwtProcessor.process(idToken, ctx);


        } catch (Exception e) {
            log.error("错误信息:{}", e);
            throw new BussinessException(ErrorCode.B01001, "ID Token 验证失败: " + e.getMessage());

        }
    }

    /**
     * 生成用户授权URL，将用户重定向到google登录页面进行身份验证
     * @return
     * @throws GeneralSecurityException
     */
    public String getGoogleAuthorizingUrl() throws GeneralSecurityException, IOException {
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
     * 生成用户授权URL，将用户重定向到github登录页面进行身份验证
     * @return
     */
    @Override
    public String getGithubAuthorizingUrl() {

//        String redirectUrl = githubAuthorization.getRedirectUrl();
        String clientId = githubAuthorization.getClientId();
        String scope = githubAuthorization.getScope();
        String url ="https://github.com/login/oauth/authorize?client_id="+clientId+"&scope="+scope;

        return url;
    }



    /**
     * 使用授权码获得登录token【google】
     * @param authorizationCode
     */
    public UserLoginVO authorizingWithCode(String authorizationCode) throws GeneralSecurityException, IOException {
        //安全性验证
        if (StringUtils.isBlank(authorizationCode)) {
            throw new IllegalArgumentException(MessageConstant.NOT_AUTHED);
        }
        // 限制授权码长度，防止潜在的恶意输入
        if (authorizationCode.length() > 2048) {
            throw new IllegalArgumentException(MessageConstant.CODE_TOO_LONG);
        }



        // 创建请求凭证
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
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

        UserLoginVO userLoginVO = loginLogic(email,username,avatar,refreshToken);

        return userLoginVO;
    }
    /**
     * 使用授权码获得登录token【github】
     * @param
     * @return
     */
    @Override
    public UserLoginVO authorizingWithGithubCode(String authorizationCode) throws JsonProcessingException {
        //安全性验证
        if (StringUtils.isBlank(authorizationCode)) {
            throw new IllegalArgumentException(MessageConstant.NOT_AUTHED);
        }
        // 限制授权码长度，防止潜在的恶意输入
        if (authorizationCode.length() > 2048) {
            throw new IllegalArgumentException(MessageConstant.CODE_TOO_LONG);
        }
        // 1. 构建请求参数（用于获取 access_token）
        RestTemplate restTemplate = new RestTemplate();


        // GitHub 要求使用 application/x-www-form-urlencoded 格式
//        HttpHeaders tokenHeaders = new HttpHeaders();
        org.springframework.http.HttpHeaders tokenHeaders = new org.springframework.http.HttpHeaders();
        //[这里要用spring的包里面的HttpHeaders，而不是google的]
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("client_id", githubAuthorization.getClientId());
        tokenParams.add("client_secret", githubAuthorization.getClientSecret());
        tokenParams.add("code", authorizationCode);
        tokenParams.add("redirect_uri", githubAuthorization.getRedirectUrl());

        // 2. 发送 POST 请求到 GitHub 获取 access_token
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

        // GitHub 的 token 交换端点
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                tokenRequest,
                String.class
        );

        // 3. 解析响应，获取 access_token
        String tokenResponseBody = tokenResponse.getBody();
        // GitHub 返回的是 query string 格式，例如：access_token=xxx&token_type=bearer&scope=read:user
        String accessToken = parseAccessTokenFromQueryString(tokenResponseBody);

        if (StringUtils.isBlank(accessToken)) {
            throw new BussinessException(ErrorCode.C01001, MessageConstant.GITHUB_AUTH_FAILED);
        }

        // 4. 使用 access_token 获取用户信息
//        HttpHeaders userHeaders = new HttpHeaders();
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

        //[需要导入org.springframework.http.HttpMethod类]
        ResponseEntity<String> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userRequest,
                String.class
        );

        // 5. 解析用户信息
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode userNode = objectMapper.readTree(userResponse.getBody());

        String email = userNode.get("email") != null ? userNode.get("email").asText() : null;
        String username = userNode.get("name") != null ? userNode.get("name").asText() : userNode.get("login").asText();
        String avatar = userNode.get("avatar_url") != null ? userNode.get("avatar_url").asText() : null;

        // 6. 调用登录逻辑
        UserLoginVO userLoginVO = loginLogic(email, username, avatar, null);

        return userLoginVO;
    }



    /**
     * 从 query string 格式解析 access_token
     * @param queryString 例如: access_token=xxx&token_type=bearer&scope=read:user
     * @return access_token 值
     */
    private String parseAccessTokenFromQueryString(String queryString) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && "access_token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }


    /**
     * 提取出两个方法共同的登录逻辑
     * @return
     */
    private UserLoginVO loginLogic(String email,String username,String avatar,String refreshToken){
        User user = userMapper.selectByEmail( email);
        if(user == null){//用户没登录过，就插入，并执行主键回填
            user = User.builder().build();
            //插入user表
            user.setUsername( username);
            user.setEmail( email);
            user.setAvatar( avatar);
            userMapper.insert(user);

        }
        //【处理status】
        if(user.getStatus()!=null && user.getStatus().equals(UserConstants.STATUS_LOCKED)){
            throw new BussinessException(ErrorCode.A01005, MessageConstant.CURRENT_USER_IS_ILLEGAL);
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


    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @Override
    public UserVO updateProfile(UserDTO userDTO) {
        //验证邮箱格式
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BussinessException(ErrorCode.A01008, MessageConstant.EMAIL_FOEMAT_ERROR);
        }

        //验证昵称长度不能超过个字30个字符
        if (userDTO.getUsername() != null && userDTO.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new BussinessException(ErrorCode.A01009, MessageConstant.USERNAME_TOO_LONG);
        }


        Long  userId = BaseContext.getCurrentId();
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        user.setId(userId);
        userMapper.update(user);
        //如果某个字段为空，是不会更新的
        User userNow = userMapper.getById(userId);
        UserVO userVO = UserVO.builder()
                .id(userNow.getId())
                .username(userNow.getUsername())
                .avatar(userNow.getAvatar())
                .gender(userNow.getGender())
                .email(userNow.getEmail())
                .build();
        return userVO;
    }

    /**
     * 修改用户密码
     * @param userDTO
     */
    @Override
    public void updatePassword(UserDTO userDTO) {
        Long currentId = BaseContext.getCurrentId();
        User user = userMapper.getById(currentId);
        //【数据库的密码是使用 BCrypt 加密的】
        if (!passwordEncoder.matches(userDTO.getCurrentPassword(), user.getPassword())) {
            throw new BussinessException(ErrorCode.A01010, MessageConstant.CURRENT_PASSWORD_ERROR);
        }
//        if (userDTO.getNewPassword().length() < 3 || userDTO.getNewPassword().length() > 20) {
//            throw new BaseException(ErrorCode.A010XX, "密码长度必须在3-20个字符之间");
//        }
        if(userDTO.getCurrentPassword().equals(userDTO.getNewPassword())){
            throw new BussinessException(ErrorCode.A01011, MessageConstant.PASSWORD_NOT_CHANGE);
        }
        // 使用 BCrypt 加密新密码
        String encodedNewPassword = passwordEncoder.encode(userDTO.getNewPassword());
        user.setPassword(encodedNewPassword);
        userMapper.update(user);
    }

    /**
     * 获取用户统计信息
     * @return
     */
    @Override
    public UserStatisticsVO getStats() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new BussinessException(ErrorCode.A01003, MessageConstant.USER_NOT_LOGIN);
        }
        Integer favoriteCount = userArticleMapper.getFavoriteCount(currentId);
        Integer likeCount = userArticleMapper.getLikeCount(currentId);
        Integer commentsCount = commentsMapper.getCommentCountByUserId(currentId);
        return UserStatisticsVO.builder()
                .commentsCount(commentsCount)
                .favoriteCount(favoriteCount)
                .likeCount(likeCount)
                .build();
    }




}
