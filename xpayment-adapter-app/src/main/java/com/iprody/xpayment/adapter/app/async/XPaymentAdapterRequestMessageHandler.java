package com.iprody.xpayment.adapter.app.async;

import com.iprody.common.async.*;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
Задача данного урока изучить взаимодействие двух приложений через Kafka поэтому
код обработки сообщений мы оставим максимально простым. Он будет просто для
каждого пришедшего сообщения отправлять ответ через 30 сек. На следующих уроках
мы дополним его необходимой бизнес логикой.
 */
@Component
public class XPaymentAdapterRequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public XPaymentAdapterRequestMessageHandler(AsyncSender<XPaymentAdapterResponseMessage> sender) {
        this.sender = sender;
    }

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        final XPaymentAdapterStatus finalStatus = (message.getAmount().toBigInteger().intValue() % 2 == 0)
            ? XPaymentAdapterStatus.SUCCEEDED
            : XPaymentAdapterStatus.CANCELED;

        scheduler.schedule(() -> {
            final XPaymentAdapterResponseMessage responseMessage =
                new XPaymentAdapterResponseMessage();
            responseMessage.setPaymentGuid(message.getPaymentGuid());
            responseMessage.setAmount(message.getAmount());
            responseMessage.setCurrency(message.getCurrency());
            responseMessage.setStatus(finalStatus);
            responseMessage.setTransactionRefId(UUID.randomUUID());
            responseMessage.setOccurredAt(OffsetDateTime.now());
            sender.send(responseMessage);
        }, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}
