package top.hazenix.service;


import top.hazenix.dto.UserLoginDTO;
import top.hazenix.entity.User;
import top.hazenix.vo.UserLoginVO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface UserService {
    String authorizingUrl() throws GeneralSecurityException, IOException;

    UserLoginVO authorizingWithCode(String code) throws GeneralSecurityException, IOException;

    UserLoginVO login(UserLoginDTO userLoginDTO);

    UserLoginVO register(UserLoginDTO userLoginDTO);

    void logout(HttpServletRequest request);
}
