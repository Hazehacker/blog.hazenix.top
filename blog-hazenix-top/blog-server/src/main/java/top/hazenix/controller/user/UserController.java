package top.hazenix.controller.user;



import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hazenix.constant.JwtClaimsConstant;
import top.hazenix.context.BaseContext;
import top.hazenix.dto.UserLoginDTO;
import top.hazenix.query.ArticleListQuery;
import top.hazenix.result.Result;
import top.hazenix.service.ArticleService;
import top.hazenix.utils.JwtUtil;
import top.hazenix.vo.ArticleDetailVO;
import top.hazenix.vo.UserLoginVO;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {
    @Autowired
    private ArticleService articleService;

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
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
//        log.info("用户登录:{}",userLoginDTO);
//        User user = userService.login(userLoginDTO);
//        HashMap<String,Object> claims = new HashMap<>();
//        claims.put(JwtClaimsConstant.USER_ID,user.getId());
//        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);
//        UserLoginVO userLoginVO = UserLoginVO.builder()
//                .id(user.getId())
//                .openid(user.getOpenid())
//                .token(token)
//                .build();
//        try {
//            return Result.success(userLoginVO);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }


}
