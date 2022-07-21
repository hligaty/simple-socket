package io.github.hligaty.util;

import io.github.hligaty.Server;
import io.github.hligaty.exception.SimpleSocketRuntimeException;
import io.github.hligaty.handler.*;
import io.github.hligaty.message.CallbackMessage;
import io.github.hligaty.message.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 消息处理器工厂，负责生成各种消息处理器
 *
 * @author hligaty
 */
public class MessageHandlerFactory {
    private static final MessageSupprot messageSupprot = new MessageSupprot();

    public static MessageHandler getMessageHandler(int code, Method method, Object controller) {
        Class<?> returnType = method.getReturnType();
        boolean isByteBuffer = parameterIsByteBuffer(method);
        if (returnType.isAssignableFrom(Message.class)) {
            return MessageHandlerFactory.getAutoSendCapableMessageHandler(code, method, controller, isByteBuffer);
        } else if (returnType.isAssignableFrom(CallbackMessage.class)) {
            return MessageHandlerFactory.getCallbackMessageHandler(code, method, controller, isByteBuffer);
        } else if ("void".equals(returnType.getName())) {
            return MessageHandlerFactory.getBaseMessageHandler(code, method, controller, isByteBuffer);
        }
        throw new SimpleSocketRuntimeException("return type must be void/Message/CallbackMessage. " + getMethodFullName(method));
    }

    public static MessageHandler getAutoSendCapableMessageHandler(int code, Method method, Object controller, boolean isByteBuffer) {
        return new AutoSendCapableMessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public Message doHandleAndWrite(ByteBuffer byteBuffer) {
                return (Message) doInvoke(method, controller, byteBuffer, isByteBuffer);
            }
        };
    }

    private static MessageHandler getCallbackMessageHandler(int code, Method method, Object controller, boolean isByteBuffer) {
        return new MessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public void doHandle(ByteBuffer byteBuffer) {
                CallbackMessage callbackMessage = (CallbackMessage) doInvoke(method, controller, byteBuffer, isByteBuffer);
                messageSupprot.send(callbackMessage, Server.getCurrentSession());
            }
        };
    }

    private static MessageHandler getBaseMessageHandler(int code, Method method, Object controller, boolean isByteBuffer) {
        return new MessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public void doHandle(ByteBuffer byteBuffer) {
                doInvoke(method, controller, byteBuffer, isByteBuffer);
            }
        };
    }

    public static LoginMessageHandler getLoginMessageHandler(int code, Method method, Object controller) {
        Assert.isTrue(!parameterIsByteBuffer(method), "parameter must be ByteBuffer. " + getMethodFullName(method));
        return new LoginMessageHandler() {
            @Override
            public Object login(ByteBuffer byteBuffer) {
                return doInvoke(method, controller, byteBuffer, true);
            }

            @Override
            public int bindCode() {
                return code;
            }
        };
    }

    public static LogoutMessageHandler getLogoutMessageHandler(int code, Method logoutMethod, Method exceptionLogoutMethod, Object controller) {
        boolean isByteBuffer = parameterIsByteBuffer(logoutMethod);
        if (exceptionLogoutMethod != null) {
            Class<?>[] parameterTypes = exceptionLogoutMethod.getParameterTypes();
            Assert.isTrue(parameterTypes.length != 2 ||
                    (parameterTypes[0].isAssignableFrom(Exception.class) && parameterTypes[1].isAssignableFrom(Message.class)),
                    "parameter must be Exception and Message. " + getMethodFullName(exceptionLogoutMethod));
        }
        return new LogoutMessageHandler() {
            @Override
            public void logout(ByteBuffer byteBuffer) {
                doInvoke(logoutMethod, controller, byteBuffer, isByteBuffer);
            }

            @Override
            public void exceptionLogout(Exception e, Message message) {
                if (exceptionLogoutMethod != null) {
                    try {
                        exceptionLogoutMethod.invoke(controller, e, message);
                    } catch (IllegalAccessException e1) {
                        throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                    } catch (InvocationTargetException e1) {
                        throw (SimpleSocketRuntimeException) e1.getTargetException();
                    }
                }
            }

            @Override
            public int bindCode() {
                return code;
            }
        };
    }

    private static boolean parameterIsByteBuffer(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean isByteBuffer = parameterTypes.length != 0 && parameterTypes[0].isAssignableFrom(ByteBuffer.class);
        Assert.isTrue(!isByteBuffer && !"void".equals(method.getReturnType().getName()), "parameter must be null/ByteBuffer. " + getMethodFullName(method));
        return isByteBuffer;
    }

    private static String getMethodFullName(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName() + "(" +
                Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")) +
                ")";
    }

    private static Object doInvoke(Method method, Object controller, ByteBuffer byteBuffer, boolean isByteBuffer) {
        try {
            if (isByteBuffer) {
                return method.invoke(controller, byteBuffer);
            } else {
                return method.invoke(controller);
            }
        } catch (IllegalAccessException e) {
            throw new SimpleSocketRuntimeException("failed to invoke doHandle");
        } catch (InvocationTargetException e) {
            throw (SimpleSocketRuntimeException) e.getTargetException();
        }
    }

}
