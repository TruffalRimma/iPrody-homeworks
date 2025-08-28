package com.iprody.payment.service.app.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentNoteUpdateDto {

    @NotNull
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
