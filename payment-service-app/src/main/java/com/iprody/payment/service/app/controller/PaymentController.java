package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.service.PaymentService;
import com.iprody.payment.service.app.service.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{guid}")
    public PaymentDto getPaymentByGuid(@PathVariable UUID guid) {
        return paymentService.get(guid);
    }

    @GetMapping
    public List<PaymentDto> getPayments() {
        return paymentService.getAll();
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
    public Page<PaymentDto> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return paymentService.search(filter, page, size, sortBy, direction);
    }
}
