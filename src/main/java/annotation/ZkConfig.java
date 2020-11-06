package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create by mirror on 2020/11/5
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
public @interface ZkConfig {
    /**
     * 注册中心 zookeeper host
     *
     * @return
     */
    String zkHost() default "";

//    /**
//     * 注册中心 zookeeper port
//     *
//     * @return
//     */
//    int zkPort() default -1;
}
