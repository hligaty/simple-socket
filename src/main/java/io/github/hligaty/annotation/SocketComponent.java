package io.github.hligaty.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标记它是一个 MessageHandler
 * 这个类必须实现 {@link io.github.hligaty.handler.MessageHandler} 接口或它的实现类
 *
 * @author hligaty
 */
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
