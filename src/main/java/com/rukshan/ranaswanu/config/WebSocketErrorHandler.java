package com.rukshan.ranaswanu.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;


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
