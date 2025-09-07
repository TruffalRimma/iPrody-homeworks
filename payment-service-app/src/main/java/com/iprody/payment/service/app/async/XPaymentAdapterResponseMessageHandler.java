package com.iprody.payment.service.app.async;

import com.iprody.common.async.MessageHandler;
import com.iprody.common.async.XPaymentAdapterResponseMessage;
import com.iprody.payment.service.app.exception.EntityNotFoundException;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
Данный класс играет роль обработчика входящих сообщений о завершении обработки
платежной транзакции XPaymentAdapter. Реализация заключается в
обновлении уже существующей записи типа данных Payment на основании
принятого сообщения.
 */
@Component
public class XPaymentAdapterResponseMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(XPaymentAdapterResponseMessageHandler.class);

    private final PaymentRepository paymentRepository;

    @Autowired
    public XPaymentAdapterResponseMessageHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void handle(XPaymentAdapterResponseMessage response) {
        log.info("Got response {}", response);

        final Payment updated = paymentRepository.findById(response.getPaymentGuid())
            .orElseThrow(() -> new EntityNotFoundException("Платеж не найден", "updateTransactionRefId",
            response.getPaymentGuid()));

        updated.setTransactionRefId(response.getTransactionRefId());
        paymentRepository.save(updated);

        log.debug("Payment's (guid = {}) field transactionRefId was successfully updated to {}",
            response.getPaymentGuid(), response.getTransactionRefId());
    }
}
