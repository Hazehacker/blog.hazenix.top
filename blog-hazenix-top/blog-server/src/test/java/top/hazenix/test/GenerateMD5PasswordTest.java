package top.hazenix.test;

import org.junit.Test;
import org.springframework.util.DigestUtils;

public class GenerateMD5PasswordTest {
    @Test
    public void passwordTest() {
        String password = "Hazenixbzh66";
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(md5Password);
    }
}
