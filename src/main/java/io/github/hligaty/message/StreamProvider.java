package io.github.hligaty.message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 流消息的回调
 *
 * @author hligaty
 */
@FunctionalInterface
public interface StreamProvider {
    /**
     * @param outputStream Socket 的发送输出流，禁止它的调用 flush 方法
     * @throws IOException 写入时发生 IO 异常
     */
    void send(OutputStream outputStream) throws IOException;
}
