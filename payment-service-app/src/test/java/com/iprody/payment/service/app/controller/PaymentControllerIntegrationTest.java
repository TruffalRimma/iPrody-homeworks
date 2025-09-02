package com.iprody.payment.service.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iprody.payment.service.app.AbstractPostgresIntegrationTest;
import com.iprody.payment.service.app.TestJwtFactory;
import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
Аннотация @AutoConfigureMockMvc указывает на то, что при запуске теста будет
создано очень близкое к реальному веб-окружение, в котором можно отправлять на
REST-endpoints приложения запросы, идентичные реальным.

Следует отметить, что интеграционные тесты не пишутся для всех возможных
вариантов, как unit-тесты. Это довольно сложно и такое количество интеграционных
тестов будет выполняться очень долго. При помощи интеграционных тестов проверяют
● успешные сценарии (happy path, sunny day scenario) для каждой операции
● важные сценарии, которые не получается покрыть unit-тестами
 */
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPayments_shouldReturnLiquibasePayments() throws Exception {
        // given + when
        mockMvc.perform(get("/api/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.guid=='00000000-0000-0000-0000-000000000001')]").exists())
                .andExpect(jsonPath("$.[?(@.guid=='00000000-0000-0000-0000-000000000002')]").exists())
                .andExpect(jsonPath("$.[?(@.guid=='00000000-0000-0000-0000-000000000003')]").exists());
    }

    @Test
    void create_shouldCreatePaymentAndVerifyInDatabase() throws Exception {
        // given
        PaymentDto dto = new PaymentDto();
        dto.setGuid(UUID.randomUUID());
        dto.setInquiryRefId(UUID.randomUUID());
        dto.setAmount(new BigDecimal("123.45"));
        dto.setCurrency("EUR");
        dto.setTransactionRefId(UUID.randomUUID());
        dto.setStatus(PaymentStatus.PENDING);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setUpdatedAt(OffsetDateTime.now());

        String json = objectMapper.writeValueAsString(dto);

        // when
        String response = mockMvc.perform(post("/api/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
        // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guid").exists())
                .andExpect(jsonPath("$.inquiryRefId").exists())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(123.45))
                .andExpect(jsonPath("$.transactionRefId").exists())
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentDto created = objectMapper.readValue(response, PaymentDto.class);
        Optional<Payment> saved = paymentRepository.findById(created.getGuid());

        assertThat(saved).isPresent();
        assertThat(saved.get().getCurrency()).isEqualTo("EUR");
        assertThat(saved.get().getAmount()).isEqualByComparingTo("123.45");
    }

    @Test
    void getPaymentByGuid_shouldReturnPaymentByGuid() throws Exception {
        // given
        UUID existingGuid = UUID.fromString("00000000-0000-0000-0000-000000000002");

        // when
        mockMvc.perform(get("/api/payments/" + existingGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingGuid.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void getPaymentByGuid_shouldReturn404ForNonexistentPayment() throws Exception {
        // given
        UUID nonexistentGuid = UUID.randomUUID();

        // when
        mockMvc.perform(get("/api/payments/" + nonexistentGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorMessage").value("Платеж не найден"))
                .andExpect(jsonPath("$.operation").value("get"))
                .andExpect(jsonPath("$.entityId").value(nonexistentGuid.toString()));
    }

    @Test
    void search_shouldReturnThirdPayment() throws Exception {
        // given + when
        mockMvc.perform(get("/api/payments/search")
                        .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                        .param("status", "DECLINED")
                        .param("currency", "CZK")
                        .param("minAmount", "10")
                        .param("maxAmount", "11")
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].guid").exists())
                .andExpect(jsonPath("$.content[0].inquiryRefId").exists())
                .andExpect(jsonPath("$.content[0].currency").value("CZK"))
                .andExpect(jsonPath("$.content[0].amount").value(10.50))
                .andExpect(jsonPath("$.content[0].status").value(PaymentStatus.DECLINED.name()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void update_shouldUpdateInfoInExistingPayment() throws Exception {
        // given
        UUID existingGuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        PaymentDto dto = new PaymentDto();
        dto.setGuid(existingGuid);
        dto.setInquiryRefId(UUID.randomUUID());
        dto.setAmount(new BigDecimal("1.45"));
        dto.setCurrency("RUB");
        dto.setTransactionRefId(UUID.randomUUID());
        dto.setStatus(PaymentStatus.NOT_SENT);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setUpdatedAt(OffsetDateTime.now());

        String json = objectMapper.writeValueAsString(dto);

        // when
        String response = mockMvc.perform(put("/api/payments/" + existingGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingGuid.toString()))
                .andExpect(jsonPath("$.inquiryRefId").value(dto.getInquiryRefId().toString()))
                .andExpect(jsonPath("$.currency").value("RUB"))
                .andExpect(jsonPath("$.amount").value("1.45"))
                .andExpect(jsonPath("$.transactionRefId").value(dto.getTransactionRefId().toString()))
                .andExpect(jsonPath("$.status").value(PaymentStatus.NOT_SENT.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentDto updated = objectMapper.readValue(response, PaymentDto.class);
        Optional<Payment> saved = paymentRepository.findById(updated.getGuid());

        assertThat(saved).isPresent();
        assertThat(saved.get().getGuid()).isEqualTo(existingGuid);
        assertThat(saved.get().getInquiryRefId()).isNotEqualTo(UUID.fromString("10000000-0000-0000-0000-000000000001"));
        assertThat(saved.get().getCurrency()).isNotEqualTo("USD");
        assertThat(saved.get().getTransactionRefId()).isNotEqualTo(UUID.fromString("20000000-0000-0000-0000-000000000001"));
        assertThat(saved.get().getStatus()).isNotEqualTo(PaymentStatus.RECEIVED);
        assertThat(saved.get().getNote()).isNotEqualTo("Test payment 1");
    }

    @Test
    void update_shouldReturn404ForNonexistentPayment() throws Exception {
        // given
        UUID nonexistentGuid = UUID.randomUUID();

        // when
        mockMvc.perform(put("/api/payments/" + nonexistentGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentDto())))
                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorMessage").value("Платеж не найден"))
                .andExpect(jsonPath("$.operation").value("update"))
                .andExpect(jsonPath("$.entityId").value(nonexistentGuid.toString()));
    }

    @Test
    void updateNote_shouldUpdateNoteInExistingPayment() throws Exception {
        // given
        UUID existingGuid = UUID.fromString("00000000-0000-0000-0000-000000000003");

        PaymentNoteUpdateDto dto = new PaymentNoteUpdateDto();
        dto.setNote("New note");

        String json = objectMapper.writeValueAsString(dto);

        // when
        String response = mockMvc.perform(patch("/api/payments/" + existingGuid + "/note")
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
        // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("New note"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        PaymentDto updated = objectMapper.readValue(response, PaymentDto.class);
        Optional<Payment> saved = paymentRepository.findById(updated.getGuid());

        assertThat(saved).isPresent();
        assertThat(saved.get().getNote()).isEqualTo("New note");
    }

    @Test
    void updateNote_shouldReturn404ForNonexistentPayment() throws Exception {
        // given
        UUID nonexistentGuid = UUID.randomUUID();

        // when
        mockMvc.perform(patch("/api/payments/" + nonexistentGuid + "/note")
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentNoteUpdateDto())))
                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorMessage").value("Платеж не найден"))
                .andExpect(jsonPath("$.operation").value("updateNote"))
                .andExpect(jsonPath("$.entityId").value(nonexistentGuid.toString()));
    }

    @Test
    void delete_shouldDeletePaymentByGuid() throws Exception {
        // given
        UUID existingGuid = UUID.fromString("00000000-0000-0000-0000-000000000002");

        // when + then
        mockMvc.perform(delete("/api/payments/" + existingGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/payments")
                        .with(TestJwtFactory.jwtWithRole("test-user2", "reader"))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[?(@.guid=='00000000-0000-0000-0000-000000000002')]").doesNotExist());
    }

    @Test
    void delete_shouldReturn404ForNonexistentPayment() throws Exception {
        // given
        UUID nonexistentGuid = UUID.randomUUID();

        // when
        mockMvc.perform(delete("/api/payments/" + nonexistentGuid)
                        .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.errorMessage").value("Платеж не найден"))
                .andExpect(jsonPath("$.operation").value("delete"))
                .andExpect(jsonPath("$.entityId").value(nonexistentGuid.toString()));
    }
}
