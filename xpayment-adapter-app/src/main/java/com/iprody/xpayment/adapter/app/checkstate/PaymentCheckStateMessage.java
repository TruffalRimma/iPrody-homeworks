package com.iprody.xpayment.adapter.app.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

// Класс - сообщение о необходимости проверить статус
public class PaymentCheckStateMessage {

    // идентификатор платежа в системе X Payment Provider
    private UUID chargeGuid;
    // идентификатор платежа в приложении Payment Service
    private UUID paymentGuid;
    private BigDecimal amount;
    private String currency;

    public PaymentCheckStateMessage(UUID chargeGuid, UUID paymentGuid, BigDecimal amount, String currency) {
        this.chargeGuid = chargeGuid;
        this.paymentGuid = paymentGuid;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getChargeGuid() {
        return chargeGuid;
    }

    public void setChargeGuid(UUID chargeGuid) {
        this.chargeGuid = chargeGuid;
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
}
