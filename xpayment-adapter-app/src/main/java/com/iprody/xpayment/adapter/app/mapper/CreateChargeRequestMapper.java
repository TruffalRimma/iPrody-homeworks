package com.iprody.xpayment.adapter.app.mapper;

import com.iprody.common.async.XPaymentAdapterRequestMessage;
import com.iprody.xpayment.adapter.app.api.model.CreateChargeRequest;
import com.iprody.xpayment.adapter.app.dto.CreateChargeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreateChargeRequestMapper {

    CreateChargeRequest toEntity(CreateChargeRequestDto dto);

    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "paymentGuid", target = "order")
    @Mapping(target = "customer", expression = "java(\"empty customer\")")
    @Mapping(target = "receiptEmail", expression = "java(\"empty email\")")
    CreateChargeRequestDto toDto(XPaymentAdapterRequestMessage message);
}
