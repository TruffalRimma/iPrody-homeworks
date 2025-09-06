package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.service.PaymentService;
import com.iprody.payment.service.app.service.PaymentServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/*
@RestController - означает, что класс является REST-контроллером. Фактически @RestController это
комбинированная аннотация состоящая из аннотаций @Controller и @ResponseBody.
Добавление аннотации @ResponseBody необходимо для того, чтобы возвращаемый
методом контроллера результат напрямую преобразовывался в JSON для отправки в качестве ответа на запрос.
 */
@RestController
/*
@RequestMapping - универсальная аннотация для задания пути и/или HTTP-метода. Применяется как на
уровне класса (для базового URL), так и на уровне метода.
 */
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    /*
    @PathVariable - извлекает переменную из URL-пути и передаёт её в метод как аргумент.
     */
    @GetMapping("/{guid}")
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public PaymentDto getPaymentByGuid(@PathVariable UUID guid) {
        log.info("GET payment by guid: {}", guid);
        final PaymentDto dto = paymentService.get(guid);
        log.debug("Sending response {}", dto);
        return dto;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public List<PaymentDto> getPayments() {
        log.info("GET all payments");
        final List<PaymentDto> dtoList = paymentService.getAll();
        log.debug("Sending response List<PaymentDto> containing {} dto(s)", dtoList.size());
        return dtoList;
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
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public Page<PaymentDto> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        log.info("GET (search) payment(s) by {}, page {}, size {}, sortBy {}, direction {}",
            filter, page, size, sortBy, direction);
        final Page<PaymentDto> resultPage = paymentService.search(filter, page, size, sortBy, direction);
        log.debug("Sending response Page<PaymentDto> containing {} payment(s)", resultPage.toList().size());
        return resultPage;
    }

    /*
    @ResponseStatus - Позволяет указать HTTP-статус, который вернётся в ответе при успешном выполнении метода.

    @RequestBody - позволяет получить данные из тела HTTP-запроса и преобразовать их в Java-объект.
    Без этой аннотации содержимое запроса не будет правильно отображаться на DTO параметр.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public PaymentDto create(@RequestBody PaymentDto dto) {
        log.info("POST (create) new payment ({})", dto);
        final PaymentDto resultDto = paymentService.create(dto);
        log.debug("Payment (guid = {}) was successfully created, sending response {}",
            dto.getGuid(), resultDto);
        return resultDto;
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto update(@PathVariable UUID guid, @RequestBody PaymentDto dto) {
        log.info("PUT (update) payment's info (guid = {})", guid);
        final PaymentDto resultDto = paymentService.update(guid, dto);
        log.debug("Payment (guid = {}) was successfully updated, sending response {}", guid, resultDto);
        return resultDto;
    }

    @PatchMapping("/{guid}/note")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updateNote(@PathVariable UUID guid, @RequestBody @Valid PaymentNoteUpdateDto dto) {
        final String note = dto.getNote();
        log.info("PATCH (update) payment's note (guid = {}) to \"{}\"", guid, note);
        final PaymentDto resultDto = paymentService.updateNote(guid, note);
        log.debug("Payment's note (guid = {}) was successfully updated to \"{}\", sending response {}",
            guid, note, resultDto);
        return resultDto;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID guid) {
        log.info("DELETE payment by guid: {}", guid);
        paymentService.delete(guid);
        log.debug("Payment (guid = {}) was successfully deleted", guid);
    }
}
