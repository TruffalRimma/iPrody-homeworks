package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentFilterFactory;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
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

    public PaymentDto get(UUID guid) {
        return paymentRepository.findById(guid)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Платеж не найден: " + guid));
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

    public PaymentDto create(PaymentDto dto) {
        final Payment entity = paymentMapper.toEntity(dto);
        final Payment saved = paymentRepository.save(entity);
        return paymentMapper.toDto(saved);
    }

    public PaymentDto update(UUID guid, PaymentDto dto) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException("Платеж не найден: " + guid);
        }

        final Payment updated = paymentMapper.toEntity(dto);
        updated.setGuid(guid);
        final Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    public PaymentDto updateNote(UUID guid, String note) {
        final Payment updated = paymentRepository.findById(guid)
            .orElseThrow(() -> new EntityNotFoundException("Платеж не найден: " + guid));

        updated.setNote(note);
        final Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    public void delete(UUID guid) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException("Платеж не найден: " + guid);
        }
        paymentRepository.deleteById(guid);
    }
}
