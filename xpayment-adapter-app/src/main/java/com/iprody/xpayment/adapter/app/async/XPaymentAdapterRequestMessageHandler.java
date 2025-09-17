package com.iprody.xpayment.adapter.app.async;

import com.iprody.common.async.*;
import com.iprody.xpayment.adapter.app.api.XPaymentProviderGateway;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import com.iprody.xpayment.adapter.app.mapper.CreateChargeRequestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;

@Component
public class XPaymentAdapterRequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private static final Logger logger = LoggerFactory.getLogger(XPaymentAdapterRequestMessageHandler.class);

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;
    private final CreateChargeRequestMapper mapper;

    @Autowired
    public XPaymentAdapterRequestMessageHandler(
        XPaymentProviderGateway xPaymentProviderGateway,
        AsyncSender<XPaymentAdapterResponseMessage> asyncSender,
        CreateChargeRequestMapper mapper
    ) {
        this.xPaymentProviderGateway = xPaymentProviderGateway;
        this.asyncSender = asyncSender;
        this.mapper = mapper;
    }

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        logger.info("Payment request received paymentGuid - {}, amount - {}, currency - {}",
            message.getPaymentGuid(), message.getAmount(), message.getCurrency());

        try {
            final ChargeResponseDto chargeResponseDto =
                xPaymentProviderGateway.createCharge(mapper.toDto(message));
            logger.info("Payment request with paymentGuid - {} is sent for payment processing. Current status - ",
                chargeResponseDto.getStatus());

            final XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
            responseMessage.setPaymentGuid(chargeResponseDto.getOrder());
            responseMessage.setTransactionRefId(chargeResponseDto.getId());
            responseMessage.setAmount(chargeResponseDto.getAmount());
            responseMessage.setCurrency(chargeResponseDto.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.valueOf(chargeResponseDto.getStatus()));
            responseMessage.setOccurredAt(OffsetDateTime.now());

            asyncSender.send(responseMessage);
        } catch (RestClientException ex) {
            logger.error("Error in time of sending payment request with paymentGuid - {}",
                message.getPaymentGuid(), ex);

            final XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
            responseMessage.setPaymentGuid(message.getPaymentGuid());
            responseMessage.setAmount(message.getAmount());
            responseMessage.setCurrency(message.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.CANCELED);
            responseMessage.setOccurredAt(OffsetDateTime.now());

            asyncSender.send(responseMessage);
        }
    }
}
