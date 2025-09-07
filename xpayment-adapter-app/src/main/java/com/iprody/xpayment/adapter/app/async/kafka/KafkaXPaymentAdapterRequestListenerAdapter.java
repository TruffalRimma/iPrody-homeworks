package com.iprody.xpayment.adapter.app.async.kafka;

import com.iprody.common.async.AsyncListener;
import com.iprody.common.async.MessageHandler;
import com.iprody.common.async.XPaymentAdapterRequestMessage;
import com.iprody.xpayment.adapter.app.validation.ValidationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

@Component
public class KafkaXPaymentAdapterRequestListenerAdapter implements AsyncListener<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterRequestListenerAdapter.class);

    private final Validator validator;
    private final MessageHandler<XPaymentAdapterRequestMessage> handler;

    public KafkaXPaymentAdapterRequestListenerAdapter(
        @Qualifier("XPaymentAdapterRequestMessageValidator") Validator validator,
        MessageHandler<XPaymentAdapterRequestMessage> handler
    ) {
        this.validator = validator;
        this.handler = handler;
    }

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handle(message);
    }

    @RetryableTopic(attempts = "${app.kafka.consumer.retry}")
    @KafkaListener(topics = "${app.kafka.topics.xpayment-adapter.request}",
        groupId = "${spring.kafka.consumer.group-id}")
    public void consume(
        XPaymentAdapterRequestMessage message,
        ConsumerRecord<String, XPaymentAdapterRequestMessage> record,
        Acknowledgment ack
    ) {
        try {
            log.info("Received XPayment Adapter request: paymentGuid={}, partition={}, offset={}",
                message.getPaymentGuid(), record.partition(), record.offset());
            validator.validateObject(message)
                .failOnError(msg -> new ValidationException("Validation exception"));
            onMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error handling XPayment Adapter request for paymentGuid={}", message.getPaymentGuid(), e);
            throw e;
        }
    }

    @DltHandler
    public void handleDltPayment(XPaymentAdapterRequestMessage message,
        @Value("${app.kafka.topics.xpayment-adapter.request:xpayment-adapter.requests}") String topic
    ) {
        log.info("Event on dlt topic={}, payload={}", topic, message);
    }
}
