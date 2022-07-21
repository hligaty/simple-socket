package io.github.hligaty.annotation;

import io.github.hligaty.handler.LoginMessageHandler;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.nio.ByteBuffer;

/**
 * 标记方法为登入方法
 * 被标记的方法的参数和返回值必须与 {@link LoginMessageHandler#login(ByteBuffer)} 相同
 *
 * @author hligaty
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
