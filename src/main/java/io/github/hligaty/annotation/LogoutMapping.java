package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SocketMapping(bindCode = -1)
public @interface LogoutMapping {
    @AliasFor(
            annotation = SocketMapping.class
    )
    int bindCode();
}
