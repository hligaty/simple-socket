package io.github.hligaty.util;

import io.github.hligaty.annotation.*;
import io.github.hligaty.exception.SimpleSocketRuntimeException;
import io.github.hligaty.handler.BroadcastMessageSupport;
import io.github.hligaty.handler.MessageHandler;
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

/**
 * 注解扫描工具类
 *
 * @author hligaty
 */
public class ComponentScanUtils {

    /**
     * 获取所有可用的消息处理器
     * 1 是 SpringBoot 环境，获取所有已经添加到容器的 MessageHandler
     * 2 为所有注解实现的消息处理器生成相应的实现类
     * 2.1 是 SpringBoot 环境，生成的 MessageHandler 处理时调用 SpringBean 的方法
     * 2.2 不是 SpringBoot 环境，调用默认的无参构造方法实例化对象，生成的 MessageHandler处理时调用实例化对象的方法
     *
     * @param packages       扫描的包路径
     * @param sessionFactory session 工厂
     * @return 扫描到的消息处理器
     */
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
            if (controller.getClass().isAssignableFrom(BroadcastMessageSupport.class) ||
                    (isSpring && ClassUtils.isAssignableValue(BroadcastMessageSupport.class, controller))) {
                // 为具有广播能力的类注入广播能力需要的 SessionFactory
                ((BroadcastMessageSupport) controller).setSessionFactory(sessionFactory);
            }
            // 登出和异常登出必须在一个 @SocketController 类中，否则异常登出失效，通常这两个方法会有公用实现，也在一个类中
            int logoutCode = -1;
            Method logoutMethod = null;
            Method exceptionLogoutMethod = null;
            for (Method method : entry.getValue()) {
                // 为每个 @SocketMapping 生成 MessageHandler
                SocketMapping socketMapping = method.getAnnotation(SocketMapping.class);
                int code = socketMapping.bindCode();
                MessageHandler messageHandler = null;
                if (socketMapping instanceof LoginMapping) {
                    messageHandler = MessageHandlerFactory.getLoginMessageHandler(code, method, controller);
                } else if (socketMapping instanceof LogoutMapping) {
                    logoutCode = code;
                    logoutMethod = method;
                } else if (socketMapping instanceof ExceptionLogoutMapping) {
                    exceptionLogoutMethod = method;
                } else {
                    messageHandler = MessageHandlerFactory.getMessageHandler(code, method, controller);
                }
                if (messageHandler != null) {
                    messageHandlers.add(messageHandler);
                }
            }
            if (logoutCode != -1) {
                // 生成登出消息处理
                messageHandlers.add(MessageHandlerFactory.getLogoutMessageHandler(logoutCode, logoutMethod, exceptionLogoutMethod, controller));
            }
        }
        return messageHandlers;
    }
}
