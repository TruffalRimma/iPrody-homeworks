package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentDto get(UUID guid);
    List<PaymentDto> getAll();
    Page<PaymentDto> search(PaymentFilter filter, int page, int size,
        String sortBy, String direction);
    PaymentDto create(PaymentDto dto);
    PaymentDto update(UUID guid, PaymentDto dto);
    PaymentDto updateNote(UUID guid, String note);
    void delete(UUID guid);
}
