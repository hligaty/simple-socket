package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.stereotype.Component
public @interface SocketComponent {
    @AliasFor(
            annotation = org.springframework.stereotype.Component.class
    )
    String value() default "";
}
