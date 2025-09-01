package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.service.PaymentService;
import com.iprody.payment.service.app.service.PaymentServiceImpl;
import jakarta.validation.Valid;
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
        return paymentService.get(guid);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'reader')")
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
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public Page<PaymentDto> searchPayments(
        @ModelAttribute PaymentFilter filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return paymentService.search(filter, page, size, sortBy, direction);
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
        return paymentService.create(dto);
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto update(@PathVariable UUID guid, @RequestBody PaymentDto dto) {
        return paymentService.update(guid, dto);
    }

    @PatchMapping("/{guid}/note")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto updateNote(@PathVariable UUID guid, @RequestBody @Valid PaymentNoteUpdateDto dto) {
        return paymentService.updateNote(guid, dto.getNote());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID guid) {
        paymentService.delete(guid);
    }
}
