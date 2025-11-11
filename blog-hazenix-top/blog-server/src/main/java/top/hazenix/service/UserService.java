package top.hazenix.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import top.hazenix.dto.UserDTO;
import top.hazenix.dto.UserLoginDTO;
import top.hazenix.entity.User;
import top.hazenix.vo.UserLoginVO;
import top.hazenix.vo.UserStatisticsVO;
import top.hazenix.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public interface UserService {
    String getGoogleAuthorizingUrl() throws GeneralSecurityException, IOException;

    UserLoginVO authorizingWithCode(String code) throws GeneralSecurityException, IOException;

    UserLoginVO login(UserLoginDTO userLoginDTO);

    UserLoginVO register(UserLoginDTO userLoginDTO);

    void logout(HttpServletRequest request);

    UserVO getUserInfo();

    UserVO updateProfile(UserDTO userDTO);

    void updatePassword(UserDTO userDTO);

    UserStatisticsVO getStats();

    String getGithubAuthorizingUrl();

    UserLoginVO authorizingWithGithubCode(String code) throws JsonProcessingException;

    UserLoginVO idTokenlogin(UserLoginDTO userLoginDTO) throws ParseException;
}
