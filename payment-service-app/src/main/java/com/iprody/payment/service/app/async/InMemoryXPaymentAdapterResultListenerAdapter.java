package com.iprody.payment.service.app.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InMemoryXPaymentAdapterResultListenerAdapter implements AsyncListener<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryXPaymentAdapterMessageHandler.class);

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    @Autowired
    public InMemoryXPaymentAdapterResultListenerAdapter(MessageHandler<XPaymentAdapterResponseMessage> handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(XPaymentAdapterResponseMessage msg) {
        log.info("Got and validated XPaymentAdapterResponseMessage, sending it to MessageHandler");
        handler.handle(msg);
    }
}
