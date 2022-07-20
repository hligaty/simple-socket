package io.github.hligaty.util;

import io.github.hligaty.Server;
import io.github.hligaty.exception.LoginException;
import io.github.hligaty.exception.SimpleSocketRuntimeException;
import io.github.hligaty.handler.*;
import io.github.hligaty.message.CallbackMessage;
import io.github.hligaty.message.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class MessageHandlerUtils {

    public static MessageHandler buildAutoSendCapableMessageHandler(Object controller, int code, Method method) {
        return new AutoSendCapableMessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public Message doHandleAndWrite(ByteBuffer byteBuffer) {
                try {
                    return (Message) method.invoke(controller, byteBuffer);
                } catch (IllegalAccessException e) {
                    throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                } catch (InvocationTargetException e) {
                    throw (SimpleSocketRuntimeException) e.getTargetException();
                }
            }
        };
    }

    public static MessageHandler buildCallbackMessageHandler(Object controller, int code, Method method, MessageHandlerSupprot messageHandlerSupprot) {
        return new MessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public void doHandle(ByteBuffer byteBuffer) {
                try {
                    messageHandlerSupprot.send((CallbackMessage) method.invoke(controller, byteBuffer), Server.getCurrentSession());
                } catch (IllegalAccessException e) {
                    throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                } catch (InvocationTargetException e) {
                    throw (SimpleSocketRuntimeException) e.getTargetException();
                }
            }
        };
    }

    public static MessageHandler buildMessageHandler(Object controller, int code, Method method) {
        return new MessageHandler() {
            @Override
            public int bindCode() {
                return code;
            }

            @Override
            public void doHandle(ByteBuffer byteBuffer) {
                try {
                    method.invoke(controller, byteBuffer);
                } catch (IllegalAccessException e) {
                    throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                } catch (InvocationTargetException e) {
                    throw (SimpleSocketRuntimeException) e.getTargetException();
                }
            }
        };
    }

    public static LoginMessageHandler buildLoginMessageHandler(Object controller, int code, Method method) {
        return new LoginMessageHandler() {
            @Override
            public Object login(ByteBuffer byteBuffer) {
                try {
                    return method.invoke(controller, byteBuffer);
                } catch (IllegalAccessException e) {
                    throw new LoginException();
                } catch (InvocationTargetException e) {
                    throw (SimpleSocketRuntimeException) e.getTargetException();
                }
            }

            @Override
            public int bindCode() {
                return code;
            }
        };
    }

    public static LogoutMessageHandler buildLogoutMessageHandler(Object controller, int code, Method logoutMethod, Method exceptionLogoutMethod) {
        return new LogoutMessageHandler() {
            @Override
            public void logout(ByteBuffer byteBuffer) {
                if (logoutMethod == null) {
                    return;
                }
                try {
                    logoutMethod.invoke(controller, byteBuffer);
                } catch (IllegalAccessException e) {
                    throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                } catch (InvocationTargetException e) {
                    throw (SimpleSocketRuntimeException) e.getTargetException();
                }
            }

            @Override
            public void exceptionLogout(Exception e, Message message) {
                if (exceptionLogoutMethod == null) {
                    return;
                }
                try {
                    exceptionLogoutMethod.invoke(controller, e, message);
                } catch (IllegalAccessException e1) {
                    throw new SimpleSocketRuntimeException("failed to invoke doHandle");
                } catch (InvocationTargetException e1) {
                    throw (SimpleSocketRuntimeException) e1.getTargetException();
                }
            }

            @Override
            public int bindCode() {
                return code;
            }
        };
    }
}
