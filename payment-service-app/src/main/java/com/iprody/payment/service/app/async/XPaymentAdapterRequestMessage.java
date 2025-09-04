package com.iprody.payment.service.app.async;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Сообщение-запрос для платёжной системы XPayment.
 * <p>
 * Используется для передачи информации о платеже, включая идентификаторы,
 * сумму, валюту и время возникновения события.
 * Реализует интерфейс {@link Message}, обеспечивая уникальный идентификатор сообщения
 * и метку времени его возникновения.
 */
public class XPaymentAdapterRequestMessage implements Message {

    /**
     * Уникальный идентификатор сообщения.
     */
    private UUID messageGuid;

    /**
     * Уникальный идентификатор платежа.
     */
    private UUID paymentGuid;

    /**
     * Сумма платежа.
     */
    private BigDecimal amount;

    /**
     * Валюта платежа в формате ISO 4217 (например, "USD", "EUR").
     */
    private String currency;

    /**
     * Момент времени, когда событие произошло.
     */
    private OffsetDateTime occurredAt;

    @Override
    public UUID getMessageId() {
        return messageGuid;
    }

    public UUID getPaymentGuid() {
        return paymentGuid;
    }

    public void setPaymentGuid(UUID paymentGuid) {
        this.paymentGuid = paymentGuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;

    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "XPaymentAdapterRequestMessage{" +
                "messageGuid=" + messageGuid +
                ", paymentGuid=" + paymentGuid +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
