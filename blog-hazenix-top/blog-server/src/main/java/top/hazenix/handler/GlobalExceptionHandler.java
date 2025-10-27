package top.hazenix.handler;

import top.hazenix.constant.MessageConstant;
import top.hazenix.exception.BaseException;
import top.hazenix.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    @ExceptionHandler
    public Result exceptionHandler(RuntimeException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获sql异常
     * @param ex
     * @return
     */
//    @ExceptionHandler
//    public Result exceptionHandler(Exception ex){
//        //对于唯一字段username，如果新增的员工的username已经在数据库中存在
//        //执行新增操作就会报一个SQLIntegrityConstraimViolationException异常
//        //并且报错信息为“Duplicate entry 'zhangsan' for key employee.idx_username”
//        String message = ex.getMessage();
//        if(message.contains("Duplicate entry")){//如果输出的日志信息包含“重复字段”，
//            //获取报错信息中的username作为提示信息的一部分
//            String[] split = message.split(" ");
//            String username = split[2];
//            String msg = username + MessageConstant.ALREADY_EXISTS;
//            return Result.error(msg);
//        }else{
//            return Result.error(MessageConstant.UNKNOWN_ERROR);
//        }
//    }

}
