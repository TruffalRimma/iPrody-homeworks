package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/{guid}")
    public ResponseEntity<Payment> getPaymentByGuid(@PathVariable UUID guid) {
        final Payment payment = paymentRepository.findById(guid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }

    /*
    @RequestParam - позволяет привязать значение одного GET-параметра из URL запроса к параметру метода обработки.
    Привязка происходит по имени параметра в методе. Если URL запроса будет таким
    http://localhost/api/payments/search&page=1, то в параметр метода page попадёт
    значение 1 из запроса. Аннотация позволяет задавать ряд дополнительных
    опций, таких как обязательность параметра (required) или значение по умолчанию (defaultValue).

    @ModelAttribute позволяет привязывать GET параметры по именам к полям DTO класса,
    такого как PaymentFilter, что очень сильно уменьшает объём кода в контроллере.
    Иначе для каждого поля из класса PaymentFilter нам пришлось бы написать отдельный параметр в методе контроллера.
     */
    @GetMapping("/search")
    public Page<Payment> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        //  descending() → Сортировка от большего к меньшему (Z → A, 100 → 1, новые → старые)
        //  ascending() → Сортировка от меньшего к большему (A → Z, 1 → 100, старые → новые)
        final Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        return paymentRepository.findAll(
                PaymentFilterFactory.fromFilter(filter),
                PageRequest.of(page, size, sort)
        );
    }
}
