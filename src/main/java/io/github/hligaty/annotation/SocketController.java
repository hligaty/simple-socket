package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标记类被扫描，就像 SpringMVC 的 @Controller 一样
 * @author hligaty
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SocketController {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
