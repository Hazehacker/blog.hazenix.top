package top.hazenix.controller.admin;



import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/admin")
public class WebController {
//    @Autowired
//    private EmployeeService employeeService;
//    private JwtProperties jwtProperties;
//
//    /**
//     * 登录
//     *
//     * @param employeeLoginDTO
//     * @return
//     */
//    @PostMapping("/login")
//    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
//        log.info("员工登录：{}", employeeLoginDTO);
//
//        Employee employee = employeeService.login(employeeLoginDTO);
//
//        //登录成功后，生成jwt令牌
//        Map<String, Object> claims = new HashMap<>();
//        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
//        String token = JwtUtil.createJWT(
//                jwtProperties.getAdminSecretKey(),
//                jwtProperties.getAdminTtl(),
//                claims);
//
//        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
//                .id(employee.getId())
//                .userName(employee.getUsername())
//                .name(employee.getName())
//                .token(token)
//                .build();
//
//        return Result.success(employeeLoginVO);
//    }

}
