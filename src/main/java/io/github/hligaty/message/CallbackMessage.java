package io.github.hligaty.message;

import io.github.hligaty.Session;
import io.github.hligaty.exception.SendException;

public class CallbackMessage {
    private final Message internalMessage;

    public CallbackMessage(Message internalMessage) {
        this.internalMessage = internalMessage;
    }

    public Message getInternalMessage() {
        return internalMessage;
    }

    public void writeCallback(Session session) {
    }

    public void exceptionCallback(SendException e, Session session) {
    }
}
