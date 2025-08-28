package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.exception.EntityNotFoundException;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentFilterFactory;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PaymentServiceTest {

    /*
    При помощи аннотации @Mock в поля paymentRepository и paymentMapper
    помещаются классы заглушки, которые библиотека Mockito создаст при помощи
    рефлексии. Далее у нас будет возможность настроить поведение этих классов нужным образом.
     */
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    // Аннотация @InjectMocks позволяет внедрять созданные заглушки в тестируемый сервис
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private UUID guid;

    @BeforeEach
    void setUp() {
        guid = UUID.randomUUID();

        payment = new Payment();
        payment.setGuid(guid);
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(BigDecimal.valueOf(123.45));
        payment.setCurrency("USD");
        payment.setTransactionRefId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setNote("Test note");
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());

        paymentDto = new PaymentDto();
        paymentDto.setGuid(payment.getGuid());
        paymentDto.setInquiryRefId(payment.getInquiryRefId());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setCurrency(payment.getCurrency());
        paymentDto.setTransactionRefId(payment.getTransactionRefId());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setNote(payment.getNote());
        paymentDto.setCreatedAt(payment.getCreatedAt());
        paymentDto.setUpdatedAt(payment.getUpdatedAt());
    }

    @Test
    void get_WhenOptionalPaymentIsNotEmpty_ReturnsPayment() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.get(guid);

        // then
        assertEquals(guid, result.getGuid());
        assertEquals(BigDecimal.valueOf(123.45), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals("Test note", result.getNote());
    }

    @Test
    void get_WhenOptionalPaymentIsEmpty_ThrowsException() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when + then
        Assertions.assertThatThrownBy(() -> paymentService.get(guid))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Платеж не найден");
    }

    @Test
    void shouldGetAllPayments() {
        // given
        Payment payment2 = new Payment();
        PaymentDto paymentDto2 = new PaymentDto();

        when(paymentRepository.findAll()).thenReturn(List.of(payment, payment2));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);
        when(paymentMapper.toDto(payment2)).thenReturn(paymentDto2);

        // when
        List<PaymentDto> result = paymentService.getAll();

        // then
        Assertions.assertThat(result).hasSize(2).contains(paymentDto, paymentDto2);
    }

    @Test
    void search_WithFullFilterPageOneAndAscAmountSorting_ReturnsPage() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setStatus(PaymentStatus.APPROVED);
        filter.setCurrency("USD");
        filter.setMinAmount(BigDecimal.ZERO);
        filter.setMaxAmount(BigDecimal.valueOf(200));
        filter.setCreatedAfter(OffsetDateTime.now().minusMinutes(2));
        filter.setCreatedBefore(OffsetDateTime.now());

        int page = 1;
        int size = 10;
        String sortBy = "amount";
        String direction = "asc";

        Page<Payment> mockPage = new PageImpl<>(Collections.singletonList(payment));
        when(paymentRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        Page<PaymentDto> result = paymentService.search(filter, page, size, sortBy, direction);

        // then
        assertEquals(1, result.getContent().size());
        assertEquals(paymentDto, result.getContent().get(0));

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        ArgumentCaptor<Specification<Payment>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(paymentRepository).findAll(specificationCaptor.capture(), pageRequestCaptor.capture());

        // проверяем, что объект Specification<Payment> не пустой
        assertNotEquals(specificationCaptor.getValue(), PaymentFilterFactory.fromFilter(new PaymentFilter()));
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.Direction.ASC, capturedPageRequest.getSort().getOrderFor(sortBy).getDirection());
    }

    @Test
    void search_WithEmptyFilterPage25AndDescCreatedAtSorting_ReturnsEmptyPage() {
        // given
        int page = 25;
        int size = 5;
        String sortBy = "createdAt";
        String direction = "desc";

        when(paymentRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // when
        Page<PaymentDto> result = paymentService.search(new PaymentFilter(), page, size, sortBy, direction);

        // then
        assertEquals(0, result.getContent().size());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        ArgumentCaptor<Specification<Payment>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(paymentRepository).findAll(specificationCaptor.capture(), pageRequestCaptor.capture());

        assertEquals(specificationCaptor.getValue(), PaymentFilterFactory.fromFilter(new PaymentFilter()));
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.Direction.DESC, capturedPageRequest.getSort().getOrderFor(sortBy).getDirection());
    }

    @Test
    void create_WhenRepositorySuccessfullySavedPaymentFromPaymentDto_ReturnsExactSamePaymentDto() {
        // given
        when(paymentMapper.toEntity(paymentDto)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.create(paymentDto);

        // then
        assertEquals(paymentDto.getGuid(), result.getGuid());
        assertEquals(paymentDto.getInquiryRefId(), result.getInquiryRefId());
        assertEquals(paymentDto.getAmount(), result.getAmount());
        assertEquals(paymentDto.getCurrency(), result.getCurrency());
        assertEquals(paymentDto.getTransactionRefId(), result.getTransactionRefId());
        assertEquals(paymentDto.getStatus(), result.getStatus());
        assertEquals(paymentDto.getNote(), result.getNote());
        assertEquals(paymentDto.getCreatedAt(), result.getCreatedAt());
        assertEquals(paymentDto.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void update_WhenPaymentExistsById_ReturnsUpdatedPaymentDto() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(true);
        when(paymentMapper.toEntity(paymentDto)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.update(guid, paymentDto);

        // then
        assertEquals(guid, result.getGuid());
        assertEquals(BigDecimal.valueOf(123.45), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals("Test note", result.getNote());
    }

    @Test
    void update_WhenPaymentDoesNotExistById_ThrowsException() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(false);

        // when + then
        Assertions.assertThatThrownBy(() -> paymentService.update(guid, paymentDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Платеж не найден");
    }

    @Test
    void updateNote_WhenOptionalPaymentIsNotEmpty_ReturnsPaymentDtoWithNewNote() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.updateNote(guid, "Test note 2");

        // then
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("Test note 2", paymentCaptor.getValue().getNote());
    }

    @Test
    void updateNote_WhenOptionalPaymentIsEmpty_ThrowsException() {
        // given
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when + then
        Assertions.assertThatThrownBy(() -> paymentService.updateNote(guid, "Test note 2"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Платеж не найден");
    }

    @Test
    void delete_WhenPaymentExistsById_DoesNotThrowException() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(true);

        // when + then
        Assertions.assertThatNoException().isThrownBy(() -> paymentService.delete(guid));
    }

    @Test
    void delete_WhenPaymentDoesNotExistById_ThrowsException() {
        // given
        when(paymentRepository.existsById(guid)).thenReturn(false);

        // when + then
        Assertions.assertThatThrownBy(() -> paymentService.delete(guid))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Платеж не найден");
    }

    /*
    Параметризованные тесты позволяют выполнять один и тот же тестовый метод с
    разными наборами данных. Это помогает избежать дублирования кода и делает тесты
    более читаемыми и масштабируемыми. Вместо написания нескольких похожих
    методов, можно использовать один параметризованный метод и передавать в него
    данные через специальный источник

    Основная аннотация:
    ● @ParameterizedTest — указывает, что метод является параметризованным тестом.

    Источники данных:
    ● @ValueSource — передаёт примитивные значения (int, String и т.д.).
    ● @CsvSource — передаёт значения в формате CSV (для нескольких параметров).
    ● @CsvFileSource — загружает данные из CSV-файла.
    ● @EnumSource — передаёт значения перечислений.
    ● @MethodSource — использует метод, возвращающий Stream, Collection или массив с аргументами.
    ● @ArgumentsSource — позволяет использовать собственный источник аргументов.
     */
    @ParameterizedTest
    @MethodSource("statusProvider")
    void should_MapDifferentPaymentStatuses(PaymentStatus status) {
        // given
        payment.setStatus(status);
        paymentDto.setStatus(status);

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.get(guid);

        // then
        assertEquals(status, result.getStatus());
    }

    static Stream<PaymentStatus> statusProvider() {
        return Stream.of(
                PaymentStatus.RECEIVED,
                PaymentStatus.PENDING,
                PaymentStatus.APPROVED,
                PaymentStatus.DECLINED,
                PaymentStatus.NOT_SENT
        );
    }
}
