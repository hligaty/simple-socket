package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标记方法为登入方法，方法必须返回一个id
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SocketMapping(bindCode = -1)
public @interface LoginMapping {
    @AliasFor(
            annotation = SocketMapping.class
    )
    int bindCode();
}
