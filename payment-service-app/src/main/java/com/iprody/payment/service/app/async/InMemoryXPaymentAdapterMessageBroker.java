package com.iprody.payment.service.app.async;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
Чтобы смоделировать взаимодействие с асинхронной системой, мы создадим класс
InMemoryXPaymentAdapterMessageBroker, реализующий интерфейс AsyncSender и
принимающий в конструкторе реализацию интерфейса AsyncListener. Метод send этого
брокера будет получать сообщения типа XPaymentAdapterRequestMessage и для
каждого такого сообщения генерировать три ответа типа
XPaymentAdapterResponseMessage с интервалом в 10 секунд. Таким образом мы
воспроизводим процесс асинхронного обновления статуса в платёжной системе.
 */
@Service
public class InMemoryXPaymentAdapterMessageBroker implements AsyncSender<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(InMemoryXPaymentAdapterMessageBroker.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;

    @Autowired
    public InMemoryXPaymentAdapterMessageBroker(AsyncListener<XPaymentAdapterResponseMessage> resultListener) {
        this.resultListener = resultListener;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        log.info("Sending request {}", request);
        final UUID txId = UUID.randomUUID();
        scheduler.schedule(() -> emit(request, txId, XPaymentAdapterStatus.PROCESSING), 0,  TimeUnit.SECONDS);
        scheduler.schedule(() -> emit(request, txId, XPaymentAdapterStatus.PROCESSING), 10, TimeUnit.SECONDS);
        final XPaymentAdapterStatus finalStatus = (request.getAmount().toBigInteger().intValue() % 2 == 0)
            ? XPaymentAdapterStatus.SUCCEEDED
            : XPaymentAdapterStatus.CANCELED;
        scheduler.schedule(() -> emit(request, txId, finalStatus), 20, TimeUnit.SECONDS);
    }

    private void emit(XPaymentAdapterRequestMessage request, UUID txId, XPaymentAdapterStatus status) {
        final XPaymentAdapterResponseMessage result = new XPaymentAdapterResponseMessage();
        result.setPaymentGuid(request.getPaymentGuid());
        result.setAmount(request.getAmount());
        result.setCurrency(request.getCurrency());
        result.setTransactionRefId(txId);
        result.setStatus(status);
        result.setOccurredAt(OffsetDateTime.now());
        resultListener.onMessage(result);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
