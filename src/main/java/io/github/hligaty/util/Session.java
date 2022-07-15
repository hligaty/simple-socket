package io.github.hligaty.util;

import io.github.hligaty.exception.ReceiveException;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.exception.SimpleSocketRuntimeException;
import io.github.hligaty.message.ByteMessage;
import io.github.hligaty.message.Message;
import io.github.hligaty.message.StreamMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hligaty
 */
public final class Session implements Closeable {
    private final Socket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(8);
    private final ReentrantLock writeLock = new ReentrantLock();
    private Object id;
    private Object attachment;
    private boolean logouted;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
        logouted = id == null;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Session(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
        this.inputStream = new BufferedInputStream(socket.getInputStream());
    }

    public void setTimeout(int timeout) {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException e) {
            throw new SimpleSocketRuntimeException();
        }
    }

    public ByteMessage receive() throws ReceiveException {
        readBuffer.clear();
        int total;
        try {
            if ((total = inputStream.read(readBuffer.array())) != 8) {
                if (total == -1) {
                    throw new ReceiveException("socket closed");
                }
                throw new ReceiveException("bad message format");
            }
            int size = readBuffer.getInt() - 4;
            int code = readBuffer.getInt();
            if (size <= 0) {
                return new ByteMessage(code);
            }
            ByteBuffer tempBuffer = ByteBuffer.allocate(size);
            if ((total = inputStream.read(tempBuffer.array(), 0, size)) != size) {
                if (total == -1) {
                    throw new ReceiveException("socket closed");
                }
                throw new ReceiveException("bad message format");
            }
            return new ByteMessage(code, tempBuffer);
        } catch (IOException e) {
            if (e instanceof ReceiveException) {
                throw (ReceiveException) e;
            }
            throw new ReceiveException("I/O error", e);
        }
    }

    public void send(Message message) throws SendException {
        writeLock.lock();
        try {
            if (message instanceof StreamMessage) {
                StreamMessage streamMessage = (StreamMessage) message;
                outputStream.write(buildTL(streamMessage).array());
                streamMessage.getSender().send(outputStream);
            } else {
                ByteMessage byteMessage = (ByteMessage) message;
                outputStream.write(buildTL(byteMessage).array());
                outputStream.write(byteMessage.getByteBuffer().array());
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new SendException("I/O error", e, message);
        } finally {
            writeLock.unlock();
        }
    }

    private ByteBuffer buildTL(Message message) {
        writeBuffer.clear();
        writeBuffer.putInt(message instanceof StreamMessage ?
                ((StreamMessage) message).getStreamSize() + 4 :
                ((ByteMessage) message).getByteBuffer().array().length + 4);
        return writeBuffer.putInt(message.getCode());
    }

    public boolean isLogouted() {
        return logouted;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
