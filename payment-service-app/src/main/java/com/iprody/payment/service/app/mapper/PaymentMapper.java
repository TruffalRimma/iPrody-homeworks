package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.Payment;
import org.mapstruct.Mapper;

/*
MapStruct будет работать с этим интерфейсом как Spring Data с репозиториями - по
аннотации над классом он поймет что нужно сгенерировать класс конвертер. Разница
только в том, что Spring Data использует рефлексию, а MapStruct будет генерировать
код класса, реализующего данный интерфейс. Благодаря параметру componentModel = "spring"
к этому классу будет добавлена аннотация, чтобы Spring создал его как бин.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto dto);
}
