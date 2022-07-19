package io.github.hligaty.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Component
public @interface Controller {
    //@AliasFor(
    //        annotation = Component.class
    //)
    //String value() default "";
}
