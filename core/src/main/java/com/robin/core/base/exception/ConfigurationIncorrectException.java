package com.robin.core.base.exception;


public class ConfigurationIncorrectException extends RuntimeException{
    public ConfigurationIncorrectException(Exception ex) {
        super(ex);
    }
    public ConfigurationIncorrectException(String description) {
        super(description);
    }
}
