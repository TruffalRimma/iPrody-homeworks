package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentFilterFactory;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentDto get(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Платеж не найден: " + id));
    }

    public List<PaymentDto> getAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::toDto).toList();
    }

    public Page<PaymentDto> search(PaymentFilter filter, int page, int size,
        String sortBy, String direction) {
        //  descending() → Сортировка от большего к меньшему (Z → A, 100 → 1, новые → старые)
        //  ascending() → Сортировка от меньшего к большему (A → Z, 1 → 100, старые → новые)
        final Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        return paymentRepository.findAll(
                PaymentFilterFactory.fromFilter(filter),
                PageRequest.of(page, size, sort)
        ).map(paymentMapper::toDto);
    }
}
