package io.github.hligaty.annotation;

import io.github.hligaty.handler.LogoutMessageHandler;
import io.github.hligaty.message.Message;

import java.lang.annotation.*;

/**
 * 标记方法为异常登出方法
 * 被标记的方法的参数必须与 {@link LogoutMessageHandler#exceptionLogout(Exception, Message)} 相同
 *
 * @author hligaty
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SocketMapping(bindCode = -1)
public @interface ExceptionLogoutMapping {
}
