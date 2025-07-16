package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.model.Payment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final Map<Long, Payment> paymentMap = new HashMap<>() {{

        for (long i = 1; i < 6; i++) {
            Payment payment = new Payment(i, 11.11 * i);
            put(payment.getId(), payment);
        }

    }};

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentMap.get(id);
    }

    @GetMapping
    public List<Payment> getPayments() {
        return paymentMap.values().stream().toList();
    }
}
