package com.iprody.xpayment.adapter.app.checkstate;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/*
Реализация интерфейса будет отправлять сообщение о необходимости
проверить статус через точку обмена с отложенной отправкой сообщения.
У сообщений в RabbitMQ есть заголовки как у сообщений в Kafka или
HTTP запросов и при помощи заголовка x-delay мы сообщаем точке отправки с какой
задержкой нужно отправить сообщение.
 */
@Service
public class PaymentStateCheckRegistrarImpl implements PaymentStateCheckRegistrar {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    @Value("${app.rabbitmq.interval-ms:60000}")
    private long intervalMs;

    @Autowired
    public PaymentStateCheckRegistrarImpl(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.delayed-exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void register(UUID chargeGuid, UUID paymentGuid, BigDecimal amount, String currency) {
        final PaymentCheckStateMessage message = new PaymentCheckStateMessage(
            chargeGuid, paymentGuid, amount, currency
        );

        rabbitTemplate.convertAndSend(
            exchangeName,
            routingKey,
            message,
            m -> {
                m.getMessageProperties().setHeader("x-delay", intervalMs);
                m.getMessageProperties().setHeader("x-retry-count", 1);
                return m;
            }
        );
    }
}
