# Simple-Socket

simple-socket 实现了一个简单的 socket 服务，可以满足“客户端->服务端”、“服务端->客户端”、“客户端->服务端->其他客户端”。

# 使用

## 消息格式

格式为 4 byte 消息长度 + 4 byte 消息码 + n byte 消息体，其中消息长度为消息码和消息体的长度和，所以当消息体为空时，消息长度为 4。

## 消息处理

实现 MessageHandler 类：bindCode() 方法的返回值是消息码； doHandle 方法处理消息，byteBuffer 是消息体。

```java
package io.github.hligaty.common.handler;

import java.nio.ByteBuffer;

/**
 * message Handler
 *
 * @author hligaty
 */
public interface MessageHandler {
    /**
     * Bind the message code that this message handler can process, cannot be repeated
     *
     * @return message code
     */
    int bindCode();

    /**
     * The message processing is implemented here, and the message body needs to be parsed by you
     *  @param byteBuffer message body
     *
     */
    void doHandle(ByteBuffer byteBuffer);
}
```

### 带回复的消息处理

实现 AutoWriteCapableMessageHandler 类，doHandlesAndWrite() 方法和前面的 MessageHandler#doHandle(ByteBuffer) 不同的是有一个返回值，返回的是回复给客户端的消息。

```java
package io.github.hligaty.common.handler;

import io.github.hligaty.handler.MessageHandler;
import io.github.hligaty.message.AbstractMessagetMessage;

import java.nio.ByteBuffer;

/**
 * Supports AutoWrite
 *
 * @author hligaty
 */
public interface AutoWriteCapableMessageHandler extends MessageHandler {

    /**
     * Auto write message.
     *
     * @see MessageHandler#doHandle(ByteBuffer)
     */
    AbstractMessage doHandlesAndWrite(ByteBuffer byteBuffer);

    //...
}
```

### 手动回复消息

手动发送消息可以在消息处理时使用  Server.getCurrentSession() 方法获取 Session，然后调用它的 send() 方法，方法会抛出 WriteException。

### 广播消息

继承 BroadcastableMessageHandler 类，并调用它的 broadcast 方法就可以对其他客户端进行广播，它共有 4 个参数（客户端 n 表示在线列表中的一个客户端，也包括发送广播消息的自己！！！）：

|                 类型                 |       名字        |              作用              |
| :----------------------------------: | :---------------: | :----------------------------: |
|               Message                |      message      |           广播的消息           |
| Predicate\<Session\> allowSend |     allowSend     | 返回 true 表示允许广播客户端 n |
|      Consumer\<Session\>       |   writeCallback   |    广播客户端 n 成功的回调     |
| BiConsumer<Exception, Session> | exceptionCallback |    广播客户端 n 异常的回调     |

broadcast 还有一个重载方法，只有第二个参数变成了 list 集合，集合元素是需要广播的客户端 id（id 在下下面的登入会提到）。

## 登入

客户端发送的第一条消息必须是登入，它继承自 BroadcastableMessageHandler，所以也有广播的能力。创建 LoginMessageHandler 的实现类，并实现 login 方法，返回值是业务 id，它用于确认连接对应的是哪个用户或哪个设备。一个简单的登入实现如下：

```java
package io.github.hligaty.demo.Handler;

import io.github.hligaty.Server;
import AbstractLoginMessageHandler;
import Message;
import Session;
import io.github.hligaty.demo.MessageCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 登录实现
 */
public class LoginMessageHandlerImpl extends AbstractLoginMessageHandler {
    private static final String ROOT = "root-";

    @Override
    public int bindCode() {
        return MessageCode.LOGIN_REQ;
    }

    @Override
    public Object login(ByteBuffer byteBuffer) {
        Message message = new Message(MessageCode.LOGIN_RESP);
        String id = new String(byteBuffer.array());
        Session session = Server.getCurrentSession();
        boolean hasLogin = false;
        if (id.startsWith(ROOT)) {
            hasLogin = true;
            session.setAttachment("whitelist users");
            message.setByteBuffer(ByteBuffer.wrap("success".getBytes(StandardCharsets.UTF_8)));
        } else {
            message.setByteBuffer(ByteBuffer.wrap("fail".getBytes(StandardCharsets.UTF_8)));
        }
        try {
            session.write(message);
        } catch (IOException e) {
            throw new RuntimeException("write error", e);
        }
        return hasLogin ? id : null;
    }
}
```

## 登出和异常登出

配合登入使用。创建 LogoutMessageHandler 的实现类，实现 logout 方法作为正常登出逻辑，实现 exceptionLogout 作为异常登出逻辑（非正常登出），二者执行其一后连接关闭。它同样继承自 BroadcastableMessageHandler，所以也有广播的能力。一个简单的实现如下：

```java
package io.github.hligaty.demo.Handler;

import io.github.hligaty.Server;
import AbstractLogoutMessageHandler;
import Message;
import Session;
import io.github.hligaty.demo.MessageCode;
import io.github.hligaty.util.Session;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class LogoutMessageHandler extends AbstractLogoutMessageHandler {

    @Override
    public int bindCode() {
        return MessageCode.LOGOUT_REQ;
    }

    @Override
    public void logout(ByteBuffer byteBuffer) {
        Session session = Server.getCurrentSession();
        log.info("{} logout", session.getId());
        Message message = new Message(MessageCode.BROADCAST, ByteBuffer.wrap(session.getId().toString().getBytes(StandardCharsets.UTF_8)));
        super.broadcast(message,
                onlineSession -> !Objects.equals(session.getId(), onlineSession.getId()),
                onlineSession -> {
                },
                (e, onlineSession) -> {
                });
    }

    @Override
    public void exceptionLogout(Exception e, Message message) {
        log.info("{}: exception logout", Server.getCurrentSession().getId());
    }
}
```

## 启动服务

指定端口号、处理连接请求的线程数、连接后的超时时间，再将消息处理器注册到服务中就完成了。

```java
Server server = new Server(new InetSocketAddress(19630), 1000 * 60, 1)
        .registerMessageHandler(new LoginMessageHandler())
        .registerMessageHandler(new LogoutMessageHandler())
        .registerMessageHandlers(Collections.singletonList(new HeartBeatMessageHandler()))
        .start();
```

# 样例

test 下的 BIOServerTest 类的 test 方法创建了 10 个客户端，登入并打印登入结果，之后客户端 10 发送登出，服务端收到消息后广播通知所有在线客户端 1`~`9，客户端 1`~`9 收到消息后再登出：

```
15:09:10.971 [main] INFO  io.github.hligaty.demo.BIOServerTest - root-7: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-4] INFO  io.github.hligaty.demo.BIOServerTest - root-4: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-1] INFO  io.github.hligaty.demo.BIOServerTest - root-10: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-9] INFO  io.github.hligaty.demo.BIOServerTest - root-3: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-15] INFO  io.github.hligaty.demo.BIOServerTest - root-6: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-6] INFO  io.github.hligaty.demo.BIOServerTest - root-9: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-8] INFO  io.github.hligaty.demo.BIOServerTest - root-8: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-2] INFO  io.github.hligaty.demo.BIOServerTest - root-5: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-11] INFO  io.github.hligaty.demo.BIOServerTest - root-2: login success
15:09:10.971 [ForkJoinPool.commonPool-worker-13] INFO  io.github.hligaty.demo.BIOServerTest - root-1: login success
15:09:10.971 [workerGroup-1] INFO  i.g.h.b.p.LogoutMessageHandlerLogoutMessageHandler - root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-9] INFO  io.github.hligaty.demo.BIOServerTest - root-3 get root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-15] INFO  io.github.hligaty.demo.BIOServerTest - root-6 get root-10 logout
15:09:10.971 [main] INFO  io.github.hligaty.demo.BIOServerTest - root-7 get root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-8] INFO  io.github.hligaty.demo.BIOServerTest - root-8 get root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-6] INFO  io.github.hligaty.demo.BIOServerTest - root-9 get root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-13] INFO  io.github.hligaty.demo.BIOServerTest - root-1 get root-10 logout
15:09:10.971 [workerGroup-4] INFO  i.g.h.b.p.LogoutMessageHandler - root-6 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-11] INFO  io.github.hligaty.demo.BIOServerTest - root-2 get root-10 logout
15:09:10.971 [workerGroup-6] INFO  i.g.h.b.p.LogoutMessageHandler - root-7 logout
15:09:10.971 [workerGroup-3] INFO  i.g.h.b.p.LogoutMessageHandler - root-9 logout
15:09:10.971 [workerGroup-5] INFO  i.g.h.b.p.LogoutMessageHandler - root-1 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-4] INFO  io.github.hligaty.demo.BIOServerTest - root-4 get root-10 logout
15:09:10.971 [ForkJoinPool.commonPool-worker-2] INFO  io.github.hligaty.demo.BIOServerTest - root-5 get root-10 logout
15:09:10.971 [workerGroup-8] INFO  i.g.h.b.p.LogoutMessageHandler - root-3 logout
15:09:10.971 [workerGroup-2] INFO  i.g.h.b.p.LogoutMessageHandler - root-8 logout
15:09:10.986 [workerGroup-7] INFO  i.g.h.b.p.LogoutMessageHandler - root-4 logout
15:09:10.971 [workerGroup-9] INFO  i.g.h.b.p.LogoutMessageHandler - root-2 logout
15:09:10.986 [workerGroup-10] INFO  i.g.h.b.p.LogoutMessageHandler - root-5 logout

Handle finished with exit code 0

```

# 其他

- 处理消息抛出 RuntimeException 不会断开连接，就像 SpringMVC 一样，处理请求出现异常一般是不会使 Tomcat 停止。注意：登入异常 LoginException 和 回复消息发送异常 AutoWriteException 除外。
- 消息有两种实现，默认的 DefaultMessage 的消息体是通过传入 byte 数组设置的，而 SenderMessage 提供 OutputStream 写入的，这个流是线程安全的。
- 处理登入和登出消息抛出的异常不会被 AbstractLogoutMessageHandler#exceptionLogout(Exception, Message) 处理。

# 未来

- 完善 readme
