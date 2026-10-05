package com.rukshan.ranaswanu.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

/**
 * Spring wraps every interceptor failure in a MessageDeliveryException whose text is
 * "Failed to send message to ExecutorSubscribableChannel[clientInboundChannel]".
 * This handler unwraps it so the browser receives the real reason
 * (e.g. "Please log in again." / "You cannot join this chat").
 */
@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable root = ex;
        while (root instanceof MessageDeliveryException && root.getCause() != null) {
            root = root.getCause();
        }
        return super.handleClientMessageProcessingError(clientMessage, root);
    }
}
