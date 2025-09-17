package com.iprody.xpayment.adapter.app.mapper;

import com.iprody.xpayment.adapter.app.api.model.ChargeResponse;
import com.iprody.xpayment.adapter.app.dto.ChargeResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChargeResponseMapper {

    ChargeResponseDto toDto(ChargeResponse entity);
}
