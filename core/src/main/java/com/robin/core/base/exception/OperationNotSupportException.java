package com.robin.core.base.exception;


public class OperationNotSupportException extends RuntimeException {
    public OperationNotSupportException(Exception ex){
        super(ex);
    }
    public OperationNotSupportException(String message){
        super(message);
    }
}
