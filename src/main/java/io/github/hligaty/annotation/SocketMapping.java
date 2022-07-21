package io.github.hligaty.annotation;

import java.lang.annotation.*;

/**
 * 标记方法是一个消息处理器，方法的类上必须有 {@link SocketController} 注解
 * 最后它将生成为一个 {@link io.github.hligaty.handler.MessageHandler} 接口及其子类的实例对象
 * 1 被标记的方法参数可以无参也可以是 {@link java.nio.ByteBuffer}，但不能是二者以外的
 * 2 被标记的方法返回值可以是 {@link io.github.hligaty.message.Message} 接口及其子类或 {@link io.github.hligaty.message.CallbackMessage}
 *
 * @author hligaty
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SocketMapping {
    /**
     * @return 处理的消息码
     */
    int bindCode();
}
