package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Create by mirror on 2020/11/3
 */
@Target({ ElementType.TYPE})
@Retention(RUNTIME)
public @interface Impl {
    /**
     * 实现哪个接口的接口类路径
     * @return
     */
    String servicePath();
}
