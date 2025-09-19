package com.iprody.xpayment.adapter.app.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

// Интерфейс - регистратор для регистрации платежа на проверку статуса
public interface PaymentStateCheckRegistrar {
    void register(UUID chargeGuid, UUID paymentGuid, BigDecimal amount, String currency);
}
