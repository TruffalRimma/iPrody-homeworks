package com.iprody.payment.service.app.persistence;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/*
Specification - это функциональный интерфейс с одним методом, включающем в себя три параметра:
    ● Root<T> root — корневой объект запроса, из которого мы получаем поля
    сущности (root.get("currency"), root.get("status") и т.д.);
    ● CriteriaQuery<?> query — модифицируемый JPA-запрос;
    ● CriteriaBuilder cb — builder, определяющий условия запроса (equal, between, gt, like и т.д.)
*/
public final class PaymentSpecifications {

    // валюта
    public static Specification<Payment> hasCurrency(String currency) {
        return (root, query, cb) -> cb.equal(root.get("currency"), currency);
    }

    // минимальная сумма
    public static Specification<Payment> minAmount(BigDecimal min) {
        return (root, query, cb) -> cb.ge(root.get("amount"), min);
    }

    // максимальная сумма
    public static Specification<Payment> maxAmount(BigDecimal max) {
        return (root, query, cb) -> cb.le(root.get("amount"), max);
    }

    // диапазон сумм
    public static Specification<Payment> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("amount"), min, max);
    }

    // создано до
    public static Specification<Payment> createdBefore(OffsetDateTime before) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), before);
    }

    // создано после
    public static Specification<Payment> createdAfter(OffsetDateTime after) {
        return (root, query, cb) -> cb.greaterThan(root.get("createdAt"), after);
    }

    // диапазон дат создания
    public static Specification<Payment> createdBetween(OffsetDateTime after, OffsetDateTime before) {
        return (root, query, cb) -> cb.between(root.get("createdAt"), after, before);
    }

    // статус
    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
