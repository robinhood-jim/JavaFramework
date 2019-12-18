package com.robin.core.base.exception;


public class ConfigurationIncorrectException extends RuntimeException{
    public ConfigurationIncorrectException(RuntimeException ex) {
        super(ex);
    }
    public ConfigurationIncorrectException(String description) {
        super(description);
    }
}
