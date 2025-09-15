package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.api.client.DefaultApi;
import com.iprody.xpayment.adapter.app.api.model.ChargeResponse;
import com.iprody.xpayment.adapter.app.api.model.CreateChargeRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
class XPaymentServiceImpl implements XPaymentService {

    private final DefaultApi defaultApi;

    public XPaymentServiceImpl(DefaultApi defaultApi) {
        this.defaultApi = defaultApi;
    }

    public ChargeResponse createCharge(CreateChargeRequest request) throws RestClientException {
        return defaultApi.createCharge(request);
    }

    public ChargeResponse retrieveCharge(UUID id) throws RestClientException {
        return defaultApi.retrieveCharge(id);
    }
}
