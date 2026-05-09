package com.assettrack.allocation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AssetAlreadyAssignedException extends RuntimeException {
    public AssetAlreadyAssignedException(String message) {
        super(message);
    }
}
