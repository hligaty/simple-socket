package io.github.hligaty.handler;

/**
 * This interface is a special purpose interface, mainly for internal use within the framework.
 * In general, application-provided message-Handlers should simply implement the plain MessageHandler(or AutoWriteCapableMessageHandler) interface
 * or derive from the AbstractMessageHandler(or BroadcastCapableMessageHandler) class.
 *
 * @author hligaty
 */
public interface SpecialMessageHandler extends MessageHandler {
}
