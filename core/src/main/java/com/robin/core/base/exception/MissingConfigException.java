package com.robin.core.base.exception;


public class MissingConfigException extends RuntimeException {
    public MissingConfigException(Exception ex) {
        super(ex);
    }
    public MissingConfigException(String message) {
        super(message);
    }
}
