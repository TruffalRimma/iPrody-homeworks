package com.iprody.payment.service.app.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/*
Для того чтобы класс был правильной Hibernate-сущностью должны выполняться
следующие условия:
1. Над ним должна быть аннотация @Entity
2. В нём должно быть поле с аннотацией @Id
3. В нём должен быть конструктор по умолчанию
4. В нём должны быть геттеры и сеттеры для всех полей, которым соответствуют
поля в таблице БД
 */

/*
@Entity - наверное самая главная аннотация Hibernate. Она указывает, что это
не просто класс, а класс-сущность, данные из которого мы хотим хранить в таблице БД
 */
@Entity
/*
@Table - позволяет указать имя таблицы в которой будут храниться данные.
Она не обязательна, но лучше явно указать имя таблицы. Если этого не сделать, то имя автоматически
генерирует Hibernate, что может привести к проблемам.
 */
@Table(name = "payment")
public class Payment {

    /*
    @Id - вторая по важности аннотация после @Entity. Она указывает, что данное
    поле является уникальным идентификатором для данной сущности и обычно
    соответствует первичному ключу таблицы в БД. Эта аннотация обязательна. Ее
    отсутствие приведет к ошибке при запуске программы
     */
    @Id
    @Column(nullable = false, unique = true)
    private UUID guid;

    /*
    @Column - нужна для указания имени и параметров полей в таблице, которым
    соответствуют поля класса. С её помощью можно задать имя поля и такие
    параметры как уникальность и возможность принимать значение null
     */
    @Column(nullable = false, name = "inquiry_ref_id")
    private UUID inquiryRefId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "transaction_ref_id")
    private UUID transactionRefId;

    /*
    @Enumerated - нужна для указания способа хранения в БД полей типа enum
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(columnDefinition = "text")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Payment() {
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public UUID getInquiryRefId() {
        return inquiryRefId;
    }

    public void setInquiryRefId(UUID inquiryRefId) {
        this.inquiryRefId = inquiryRefId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public UUID getTransactionRefId() {
        return transactionRefId;
    }

    public void setTransactionRefId(UUID transactionRefId) {
        this.transactionRefId = transactionRefId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
