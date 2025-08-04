package com.iprody.payment.service.app.persistence;

import com.iprody.payment.service.app.persistence.entity.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentFilterFactory {

    private static final Specification<Payment> EMPTY = (root, query, cb) -> null;

    public static Specification<Payment> fromFilter(PaymentFilter filter) {
        Specification<Payment> spec = EMPTY;

        if (StringUtils.hasText(filter.getCurrency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.getCurrency()));
        }

        if (filter.getMinAmount() != null) {
            spec = spec.and(PaymentSpecifications.minAmount(filter.getMinAmount()));
        }

        if (filter.getMaxAmount() != null) {
            spec = spec.and(PaymentSpecifications.maxAmount(filter.getMaxAmount()));
        }

        if (filter.getMinAmount() != null && filter.getMaxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountBetween(filter.getMinAmount(), filter.getMaxAmount()));
        }

        if (filter.getCreatedBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdBefore(filter.getCreatedBefore()));
        }

        if (filter.getCreatedAfter() != null) {
            spec = spec.and(PaymentSpecifications.createdAfter(filter.getCreatedAfter()));
        }

        if (filter.getCreatedAfter() != null && filter.getCreatedBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdBetween(filter.getCreatedAfter(), filter.getCreatedBefore()));
        }

        if (filter.getStatus() != null) {
            spec = spec.and(PaymentSpecifications.hasStatus(filter.getStatus()));
        }

        return spec;
    }
}
