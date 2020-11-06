package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create by mirror on 2020/11/3
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface RpcServiceStartConfig {
    /**
     * 扫描路径
     *
     * @return
     */
    String packagePath();

    /**
     * 服务端启动地址
     *
     * @return
     */
    String nettyHost() default "127.0.0.1";

    /**
     * 服务端启动端口
     *
     * @return
     */
    int nettyPort() default 7000;



    /**
     * 服务名称
     *
     * @return
     */
    String name();
}
