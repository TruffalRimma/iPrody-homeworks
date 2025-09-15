package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.api.model.ChargeResponse;
import com.iprody.xpayment.adapter.app.api.model.CreateChargeRequest;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

public interface XPaymentService {
    ChargeResponse createCharge(CreateChargeRequest request) throws RestClientException;
    ChargeResponse retrieveCharge(UUID id) throws RestClientException;
}
