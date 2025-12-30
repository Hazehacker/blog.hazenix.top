package top.hazenix.controller.user;



import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserDTO;
import top.hazenix.dto.UserLoginDTO;
import top.hazenix.entity.User;
import top.hazenix.properties.JwtProperties;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.service.UserService;
import top.hazenix.utils.JwtUtil;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.UserLoginVO;
import top.hazenix.vo.UserStatisticsVO;
import top.hazenix.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/user/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final ArticleService articleService;

    private final UserService userService;


    /**
     * 获取用户收藏的文章
     * @return
     */
    @GetMapping("/favorite")
    public Result getFavoriteCollections(){
        ArticleListQuery articleListQuery = ArticleListQuery.builder()
                .status(0)
                .userId(BaseContext.getCurrentId())
                .build();
        List<ArticleDetailVO> list = articleService.getArticleList(articleListQuery);
        return Result.success(list);
    }

    /**
     * 用户使用邮箱登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录:{}",userLoginDTO);
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 用户使用邮箱注册账号
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/register")
    public Result<UserLoginVO> register(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户注册:{}",userLoginDTO);
        UserLoginVO userLoginVO = userService.register(userLoginDTO);
        return Result.success(userLoginVO);
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        log.info("用户退出登录");
        userService.logout(request);
        return Result.success();
    }

    /**
     * 前端取得 Google id_token（JWT），再用这个后端接口处理登录逻辑
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/google/idtoken-login")
    public Result<UserLoginVO> googleIdTokenLogin(@RequestBody UserLoginDTO userLoginDTO) throws ParseException {
        log.info("id token登录:{}",userLoginDTO);
        UserLoginVO userLoginVO = userService.idTokenlogin(userLoginDTO);
        return Result.success(userLoginVO);
    }


    /**
     * 生成用户授权URL，将用户重定向到gooogle登录页面进行身份验证
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @GetMapping("/google/url")
    public Result getUrl() throws GeneralSecurityException, IOException {
        return Result.success(userService.getGoogleAuthorizingUrl());
    }

    /**
     * 生成用户授权URL，将用户重定向到github登录页面进行身份验证
     * （暂时不采用，直接前端跳转）
     * @return
     */
    @GetMapping("/github/url")
    public Result getGithubUrl(){
        return Result.success(userService.getGithubAuthorizingUrl());
    }

    /**
     * 获取用户授权信息（用code换取）
     * @param code
     * @return
     */
    @GetMapping("/google/callback")
    public Result handleGoogleCallback(@RequestParam String code) throws GeneralSecurityException, IOException {
        UserLoginVO userLoginVO = userService.authorizingWithCode(code);
        return Result.success(userLoginVO);
    }

    /**
     * 使用授权码获得登录token和用户信息
     * @param code
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/github/callback")
    public Result handleGithubCallback(@RequestParam String code) throws JsonProcessingException {
        log.info("github第三方登录：{}",code);
        UserLoginVO userLoginVO = userService.authorizingWithGithubCode(code);
        return Result.success(userLoginVO);
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/userinfo")
    public Result getUserInfo(){
        log.info("获取用户信息");
        UserVO userVO = userService.getUserInfo();
        return Result.success(userVO);
    }

    /**
     * 获取用户相关统计信息
     * @return
     */
    @GetMapping("/stats")
    public Result getStats(){
        log.info("获取用户统计信息");
        UserStatisticsVO userStatisticsVO = userService.getStats();
        return Result.success(userStatisticsVO);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody UserDTO userDTO){
        log.info("更新用户信息:{}",userDTO);
        UserVO userVO = userService.updateProfile(userDTO);
        return Result.success(userVO);
    }
    @PutMapping("/password")
    public Result updatePassword(@RequestBody UserDTO userDTO){
        log.info("更新用户密码:{}",userDTO);
        userService.updatePassword(userDTO);
        return Result.success();
    }





}
