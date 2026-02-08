package top.hazenix.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private String code; // 错误码
    private String msg; // 错误信息
    private T data; // 数据
    private String requestId; // 用于追踪请求链路

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = "200";
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = "200";
        return result;
    }

    public static <T> Result<T> error(String code, String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = code;
        return result;
    }

}
