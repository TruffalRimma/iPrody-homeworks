package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.api.client.DefaultApi;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import com.iprody.xpayment.adapter.app.dto.CreateChargeRequestDto;
import com.iprody.xpayment.adapter.app.mapper.ChargeResponseMapper;
import com.iprody.xpayment.adapter.app.mapper.CreateChargeRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
class XPaymentProviderGatewayImpl implements XPaymentProviderGateway {

    private final DefaultApi defaultApi;
    private final CreateChargeRequestMapper requestMapper;
    private final ChargeResponseMapper responseMapper;

    @Autowired
    public XPaymentProviderGatewayImpl(
        DefaultApi defaultApi,
        CreateChargeRequestMapper requestMapper,
        ChargeResponseMapper responseMapper
    ) {
        this.defaultApi = defaultApi;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @Override
    public ChargeResponseDto createCharge(CreateChargeRequestDto dto) throws RestClientException {
        try {
            return responseMapper.toDto(defaultApi.createCharge(requestMapper.toEntity(dto)));
        } catch (Exception e) {
            throw new RestClientException("POST /charges failed", e);
        }
    }

    @Override
    public ChargeResponseDto retrieveCharge(UUID id) throws RestClientException {
        try {
            return responseMapper.toDto(defaultApi.retrieveCharge(id));
        } catch (Exception e) {
            throw new RestClientException("GET /charges/{id} failed (id=" + id + ")", e);
        }
    }
}
