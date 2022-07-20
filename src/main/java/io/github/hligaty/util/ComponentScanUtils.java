package io.github.hligaty.util;

import io.github.hligaty.SessionFactory;
import io.github.hligaty.annotation.*;
import io.github.hligaty.exception.SimpleSocketRuntimeException;
import io.github.hligaty.handler.BroadcastCapableMessageHandlerSupport;
import io.github.hligaty.handler.MessageHandler;
import io.github.hligaty.handler.MessageHandlerSupprot;
import io.github.hligaty.message.CallbackMessage;
import io.github.hligaty.message.Message;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ComponentScanUtils {

    public static Collection<MessageHandler> annotationScan(String packages, SessionFactory sessionFactory) {
        List<MessageHandler> messageHandlers = new ArrayList<>();
        boolean isSpring = false;
        try {
            Class.forName("org.springframework.context.ApplicationContext");
            if (ApplicationContextUtils.getApplicationContext() != null) {
                isSpring = true;
                packages = ClassUtils.getPackageName(ApplicationContextUtils.getApplicationContext().getBeansWithAnnotation(SpringBootApplication.class).values().stream()
                        .findFirst().orElseThrow(() -> new SimpleSocketRuntimeException("not found main class")).getClass());
                // 注册 Spring 容器中的 MessageHandler Bean
                messageHandlers.addAll(ApplicationContextUtils.getApplicationContext().getBeansOfType(MessageHandler.class).values());
            }
        } catch (ClassNotFoundException ignored) {
        }
        Reflections reflections = new Reflections(packages, Scanners.values());
        if (!isSpring) {
            // 没有 Spring 容器，扫描有 SocketComponent 注解的类，实例化并添加到委托消息处理
            for (Class<?> messageHandlerClass : reflections.get(TypesAnnotated.with(SocketComponent.class).asClass())) {
                try {
                    MessageHandler messageHandler = (MessageHandler) messageHandlerClass.newInstance();
                    messageHandlers.add(messageHandler);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new SimpleSocketRuntimeException("failed to create instance");
                }
            }
        }
        // 扫描所有通过 @Mapping 实现 MessageHandler 的类
        Map<? extends Class<?>, ArrayList<Method>> controllerClasses = reflections.get(TypesAnnotated.with(SocketController.class).asClass()).stream()
                .collect(Collectors.toMap(controllerClass -> controllerClass, controllerClass -> new ArrayList<>()));
        // 添加到对应的 @SocketController 中，类似 SpringBoot 的 @ConfigurationClass，然后再解析
        reflections.get(MethodsAnnotated.with(SocketMapping.class).as(Method.class))
                .forEach(method -> controllerClasses.get(method.getDeclaringClass()).add(method));
        MessageHandlerSupprot messageHandlerSupprot = new MessageHandlerSupprot();
        // 处理每个 @SocketController 类
        for (Map.Entry<? extends Class<?>, ArrayList<Method>> entry : controllerClasses.entrySet()) {
            Class<?> controllerClass = entry.getKey();
            Object controller;
            try {
                controller = isSpring ?
                        ApplicationContextUtils.getBean(controllerClass) :
                        controllerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new SimpleSocketRuntimeException("failed to create instance");
            }
            if (controller.getClass().isAssignableFrom(BroadcastCapableMessageHandlerSupport.class) ||
                    (isSpring && ClassUtils.isAssignableValue(BroadcastCapableMessageHandlerSupport.class, controller))) {
                // 为具有广播能力的类注入广播能力需要的 SessionFactory
                ((BroadcastCapableMessageHandlerSupport) controller).setSessionFactory(sessionFactory);
            }
            // 登出和异常登出必须在一个 @SocketController 类中，否则异常登出失效，通常这两个方法会有公用实现，也在一个类中
            int logoutCode = -1;
            Method logoutMethod = null;
            Method exceptionLogoutMethod = null;
            for (Method method : entry.getValue()) {
                // 为每个 @SocketMapping 生成 MessageHandler
                // TODO: 2022/7/20 还需要匹配方法参数，一般都要接收 ByteBuffer，但有的消息不需要，还需要适配
                SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);
                int code = socketMapping.bindCode();
                Class<?> returnType = method.getReturnType();
                MessageHandler messageHandler = null;
                if (socketMapping instanceof LoginMapping) {
                    messageHandler = MessageHandlerUtils.buildLoginMessageHandler(controller, code, method);
                } else if (socketMapping instanceof LogoutMapping) {
                    logoutCode = code;
                    logoutMethod = method;
                } else if (socketMapping instanceof ExceptionLogoutMapping) {
                    exceptionLogoutMethod = method;
                } else {
                    if (returnType.isAssignableFrom(Message.class)) {
                        messageHandler = MessageHandlerUtils.buildAutoSendCapableMessageHandler(controller, code, method);
                    } else if (returnType.isAssignableFrom(CallbackMessage.class)) {
                        messageHandler = MessageHandlerUtils.buildCallbackMessageHandler(controller, code, method, messageHandlerSupprot);
                    } else if ("void".equals(returnType.getName())) {
                        messageHandler = MessageHandlerUtils.buildMessageHandler(controller, code, method);
                    }
                }
                if (messageHandler != null) {
                    messageHandlers.add(messageHandler);
                }
            }
            if (logoutCode != -1) {
                // 生成登出消息处理
                messageHandlers.add(MessageHandlerUtils.buildLogoutMessageHandler(controller, logoutCode, logoutMethod, exceptionLogoutMethod));
            }
        }
        return messageHandlers;
    }
}
