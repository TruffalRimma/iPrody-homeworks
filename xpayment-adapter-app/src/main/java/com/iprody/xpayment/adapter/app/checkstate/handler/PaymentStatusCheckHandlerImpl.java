package com.iprody.xpayment.adapter.app.checkstate.handler;

import com.iprody.common.async.AsyncSender;
import com.iprody.common.async.XPaymentAdapterResponseMessage;
import com.iprody.common.async.XPaymentAdapterStatus;
import com.iprody.xpayment.adapter.app.api.XPaymentProviderGateway;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class PaymentStatusCheckHandlerImpl implements PaymentStatusCheckHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusCheckHandlerImpl.class);

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;

    @Autowired
    public PaymentStatusCheckHandlerImpl(
        XPaymentProviderGateway xPaymentProviderGateway,
        AsyncSender<XPaymentAdapterResponseMessage> asyncSender
    ) {
        this.xPaymentProviderGateway = xPaymentProviderGateway;
        this.asyncSender = asyncSender;
    }

    @Override
    public boolean handle(UUID chargeGuid) throws RestClientException {
        logger.info("Sending request to check charge status(guid = {}) ", chargeGuid);

        try {
            final ChargeResponseDto chargeResponseDto = xPaymentProviderGateway.retrieveCharge(chargeGuid);
            final String status = chargeResponseDto.getStatus();

            logger.info("Current charge (guid = {}) status -> {}", chargeGuid, chargeResponseDto.getStatus());

            if (XPaymentAdapterStatus.PROCESSING.name().equals(status)) {
                return false;
            }

            final XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
            responseMessage.setPaymentGuid(chargeResponseDto.getOrder());
            responseMessage.setTransactionRefId(chargeResponseDto.getId());
            responseMessage.setAmount(chargeResponseDto.getAmount());
            responseMessage.setCurrency(chargeResponseDto.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.valueOf(chargeResponseDto.getStatus()));
            responseMessage.setOccurredAt(OffsetDateTime.now());

            asyncSender.send(responseMessage);
            return true;
        } catch (RestClientException ex) {
            logger.error("Error in time of sending charge request with chargeGuid - {}", chargeGuid, ex);
            throw ex;
        }
    }
}
