package io.github.hligaty.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SocketMapping(bindCode = -1)
public @interface ExceptionLogoutMapping {
}
