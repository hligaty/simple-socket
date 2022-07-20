package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

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
