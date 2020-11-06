package common;

import java.io.Serializable;

/**
 * Create by mirror on 2020/11/2
 */
public enum StatusCode implements Serializable {
    SUCCESS("成功", 9000),
    PROTOCOL_COMMON("协议错误", 8000),
    NON_IMPL("没有实现类", 8000);

    public final String errMsg;
    public final int code;
    StatusCode(String errMsg, int code) {
        this.errMsg = errMsg;
        this.code = code;
    }

}
