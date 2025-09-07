package com.iprody.xpayment.adapter.app.validation;

import com.iprody.common.async.XPaymentAdapterRequestMessage;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class XPaymentAdapterRequestMessageValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return XPaymentAdapterRequestMessage.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final XPaymentAdapterRequestMessage message = (XPaymentAdapterRequestMessage) target;

        // Сумма и валюта всегда должны присутствовать
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currency", "field.required");

        // Сумма не может быть отрицательной
        if (message.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue("amount", "field.not.negative");
        }

        // Количество знаков после запятой в сумме должно соответствовать требованиям валюты (см. ISO 4217)
        if (Currency.getInstance(message.getCurrency()).getDefaultFractionDigits() != message.getAmount().scale()) {
            errors.rejectValue("amount", "currency.to.amount.iso4271");
        }
    }
}
