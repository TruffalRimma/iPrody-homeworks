package com.iprody.payment.service.app.exception;

import java.time.Instant;
import java.util.UUID;

public record ErrorDto(Instant timestamp, int errorCode, String errorMessage, String operation, UUID entityId) {

    public ErrorDto(int errorCode, String errorMessage, String operation, UUID entityId) {
        this(Instant.now(), errorCode, errorMessage, operation, entityId);
    }
}
