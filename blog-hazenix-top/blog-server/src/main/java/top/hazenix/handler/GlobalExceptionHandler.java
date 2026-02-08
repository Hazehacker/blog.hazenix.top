package top.hazenix.handler;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.hazenix.constant.ErrorCode;
import top.hazenix.exception.BussinessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import top.hazenix.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(BussinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBussinessException(BussinessException e, HttpServletRequest request){
        String requestId = getRequestId();

        // 记录 Warn 级别日志，包含请求URL、错误码和错误信息
        log.warn("业务异常 | RequestId: {} | URI: {} | Code: {} | Error: {}",
                requestId, request.getRequestURI(), e.getCode(), e.getMessage());

        // 返回给前端：使用业务异常自带的 code (字符串错误码) 和 message
        Result<Object> result = Result.error(e.getCode(), e.getMessage());
        result.setRequestId(requestId);
        result.setData(e.getData()); // 如果有附加数据也返回
        return result;
    }

    // 用于在日志和返回中标识请求
    private String getRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 处理参数校验异常 (Spring Validated)
     * 当 @Valid 注解校验失败时触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestId = getRequestId();
        log.warn("参数校验异常 | RequestId: {} | URI: {} | Code: {} | Error: {}", 
                requestId, request.getRequestURI(), ErrorCode.A00001, e.getMessage());

        // 提取具体的字段错误信息
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Result<Map<String, String>> result = Result.error(ErrorCode.A00001, "参数验证失败");
        result.setRequestId(requestId);
        result.setData(errors);
        return result;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exceptionHandler(Exception e, HttpServletRequest request){
        String requestId = getRequestId();

        // ERROR 级别日志，记录完整堆栈，用于排查Bug
        log.error("系统内部错误 | RequestId: {} | URI: {} | Code: {} | IP: {}",
                requestId, request.getRequestURI(), "B00001", getClientIpAddress(request), e);

        // 响应给前端：隐藏具体细节，只提示"系统繁忙"
        Result<String> result = Result.error("B00001", "系统繁忙，请稍后再试");
        result.setRequestId(requestId);
        return result;
    }

    /**
     * 获取客户端真实IP地址（考虑了反向代理的情况）
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }



}
