package com.iprody.xpayment.adapter.app.checkstate;

import com.iprody.xpayment.adapter.app.checkstate.handler.PaymentStatusCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/*
Основной задачей класса является прослушка входящих сообщений из основной очереди RabbitMQ
с последующей операцией уточнения статуса платежа (информация о котором содержится в полученном сообщении)
через PaymentStatusCheckHandler.handle(...)
 */
@Component
public class PaymentStateCheckListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStateCheckListener.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final String dlxExchangeName;
    private final String dlxRoutingKey;
    private final PaymentStatusCheckHandler paymentStatusCheckHandler;

    @Value("${app.rabbitmq.max-retries:60}")
    private int maxRetries;

    @Value("${app.rabbitmq.interval-ms:60000}")
    private long intervalMs;

    @Autowired
    public PaymentStateCheckListener(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.delayed-exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey,
        @Value("${app.rabbitmq.dlx-exchange-name}") String dlxExchangeName,
        @Value("${app.rabbitmq.dlx-routing-key}") String dlxRoutingKey,
        PaymentStatusCheckHandler paymentStatusCheckHandler
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.dlxExchangeName = dlxExchangeName;
        this.dlxRoutingKey = dlxRoutingKey;
        this.paymentStatusCheckHandler = paymentStatusCheckHandler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handle(PaymentCheckStateMessage message, Message raw) {
        final MessageProperties props = raw.getMessageProperties();
        final int retryCount = (int) props.getHeaders().getOrDefault("x-retry-count", 1);

        try {
            logger.info("Retry №{} checking status for payment {} amount {} {}",
                retryCount, message.getPaymentGuid(), message.getAmount(), message.getCurrency());

            final boolean paid = paymentStatusCheckHandler.handle(message.getChargeGuid());

            if (paid) {
                return;
            }

            if (retryCount < maxRetries) {
                // Планируем следующую проверку
                final PaymentCheckStateMessage newMessage = new PaymentCheckStateMessage(
                    message.getChargeGuid(),
                    message.getPaymentGuid(),
                    message.getAmount(),
                    message.getCurrency()
                );

                rabbitTemplate.convertAndSend(
                    exchangeName,
                    routingKey,
                    newMessage,
                    m -> {
                        m.getMessageProperties().setHeader("x-delay", intervalMs);
                        m.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                        return m;
                    }
                );
            } else {
                // Исчерпали попытки -- кладём сообщение в DLX
                sendToDlx(message, retryCount, props.getConsumerQueue(), "TIMEOUT");
            }
        } catch (RestClientException ex) {
            sendToDlx(message, retryCount, props.getConsumerQueue(), "ERROR");
        }
    }

    private void sendToDlx(
        PaymentCheckStateMessage message,
        int retryCount,
        String queue,
        String dlxStatus
    ) {
        rabbitTemplate.convertAndSend(
            dlxExchangeName,
            dlxRoutingKey,
            message,
            m -> {
                m.getMessageProperties().setHeader("x-retry-count", retryCount);
                m.getMessageProperties().setHeader("x-final-status", dlxStatus);
                m.getMessageProperties().setHeader("x-original-queue", queue);
                return m;
            }
        );
    }
}
