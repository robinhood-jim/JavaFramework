package com.robin.core.base.exception;

public class ValueNullException extends RuntimeException{
    public ValueNullException(Throwable ex) {
        super(ex);
    }
    public ValueNullException(String errMsg) {
        super(errMsg);
    }
}
