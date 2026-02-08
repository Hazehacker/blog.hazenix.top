package top.hazenix.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 业务异常
 */
@Getter
@NoArgsConstructor
public class BussinessException extends RuntimeException {

    private String code; // 业务错误码
    private Object data; // 可以携带相关的业务数据

    // 不携带业务数据
    public BussinessException(String code, String message) {
        this(code, message, null);
    }

    // 携带业务数据
    public BussinessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }

}
