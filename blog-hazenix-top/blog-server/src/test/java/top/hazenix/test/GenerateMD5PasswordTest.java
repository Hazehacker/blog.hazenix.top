package top.hazenix.test;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 使用 BCrypt 生成密码摘要，便于本地调试或数据初始化时使用
 */
public class GenerateMD5PasswordTest {
    @Test
    public void passwordTest() {
        String password = "123456";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String bcryptPassword = passwordEncoder.encode(password);
        System.out.println(bcryptPassword);
    }
}
