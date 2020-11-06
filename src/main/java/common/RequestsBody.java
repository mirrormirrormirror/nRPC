package common;


import java.io.Serializable;

/**
 * @author mirror
 */
public class RequestsBody implements Serializable {

    /**
     * 协议头
     */
    private String protocol;

    /**
     * 类反射
     */
    private String className;


    /**
     * 调用方法名称
     */
    private String methodName;

    /**
     * 方法类型
     */
    private String methodTypes;

    /**
     * 客户端调用方法时，传入的参数
     */
    private Object[] args;


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodTypes() {
        return methodTypes;
    }

    public void setMethodTypes(String methodTypes) {
        this.methodTypes = methodTypes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

}

