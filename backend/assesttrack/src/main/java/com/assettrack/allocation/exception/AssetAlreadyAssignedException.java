package com.assettrack.allocation.exception;

public class AssetAlreadyAssignedException extends RuntimeException {
    public AssetAlreadyAssignedException(String message) {
        super(message);
    }
}
