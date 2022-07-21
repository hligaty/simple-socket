package io.github.hligaty.util;

import io.github.hligaty.exception.SimpleSocketRuntimeException;

/**
 * 断言工具类
 *
 * @author hligaty
 */
public class Assert {
    public static void isTrue(boolean expression, String message) {
        if (expression) {
            throw new SimpleSocketRuntimeException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }
}
