package com.iprody.xpayment.adapter.app.checkstate;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
Нам понадобится также очередь для сообщений при обработке которых произошла ошибка.
Например, исчерпалось количество попыток повтора. Их отправляют в т.н. Dead Letter Queue (DLX).
 */
@Configuration
public class RabbitMqDlxConfig {

    @Value("${app.rabbitmq.dlx-exchange-name}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.dead-letter-queue-name}")
    private String deadLetterQueueName;

    @Value("${app.rabbitmq.dlx-routing-key}")
    private String dlxRoutingKey;

    // создание точки обмена типа Dead Letter Message
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName);
    }

    // создание очереди сообщений о незавершенных проверках статуса платежа
    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueueName).build();
    }

    // связывание очереди и точки обмена
    @Bean
    Binding dlxBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(dlxRoutingKey);
    }
}
