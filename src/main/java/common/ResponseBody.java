package common;

import java.io.Serializable;

/**
 * @author mirror
 */
public class ResponseBody<T> implements Serializable {
    private T result;
    private StatusCode statusCode;

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public T getResult() {
        return result;
    }



    public void setResult(T result) {
        this.result = result;
    }
}
