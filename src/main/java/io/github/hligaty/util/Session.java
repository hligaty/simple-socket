package io.github.hligaty.util;

import io.github.hligaty.exception.ReceiveException;
import io.github.hligaty.exception.SendException;
import io.github.hligaty.message.Message;
import io.github.hligaty.message.DefaultMessage;
import io.github.hligaty.message.SenderMessage;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author hligaty
 */
public final class Session implements Closeable {
    private final Socket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(8);
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

    public Session(Socket socket, int timeout) throws IOException {
        this.socket = socket;
        this.socket.setSoTimeout(timeout);
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
        this.inputStream = new BufferedInputStream(socket.getInputStream());
    }

    public DefaultMessage receive() throws ReceiveException {
        readBuffer.clear();
        int total;
        try {
            if ((total = inputStream.read(readBuffer.array(), 0, 8)) != 8) {
                if (total == -1) {
                    throw new ReceiveException("socket closed");
                }
                throw new ReceiveException("bad message format");
            }
            int size = readBuffer.getInt() - 4;
            int code = readBuffer.getInt();
            if (size <= 0) {
                return new DefaultMessage(code);
            }
            ByteBuffer tempBuffer = ByteBuffer.allocate(size);
            if ((total = inputStream.read(tempBuffer.array(), 0, size)) != size) {
                if (total == -1) {
                    throw new ReceiveException("socket closed");
                }
                throw new ReceiveException("bad message format");
            }
            return new DefaultMessage(code, tempBuffer);
        } catch (IOException e) {
            if (e instanceof ReceiveException) {
                throw (ReceiveException) e;
            }
            throw new ReceiveException("I/O error", e);
        }
    }

    public void send(Message message) throws SendException {
        synchronized (outputStream) {
            try {
                if (message instanceof SenderMessage) {
                    SenderMessage senderMessage = (SenderMessage) message;
                    outputStream.write(buildTL(senderMessage).array());
                    senderMessage.getSender().send(outputStream);
                } else {
                    DefaultMessage defaultMessage = (DefaultMessage) message;
                    outputStream.write(buildTL(defaultMessage).array());
                    outputStream.write(defaultMessage.getByteBuffer().array());
                    outputStream.flush();
                }
            } catch (IOException e) {
                throw new SendException("I/O error", e, message);
            }
        }
    }

    private ByteBuffer buildTL(Message message) {
        writeBuffer.clear();
        writeBuffer.putInt(message instanceof SenderMessage ?
                ((SenderMessage) message).getBodySize() + 4:
                ((DefaultMessage) message).getByteBuffer().array().length + 4);
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
