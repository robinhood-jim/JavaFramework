package com.robin.core.base.exception;

import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.MessageUtils;

public abstract class AbstractCodeException extends RuntimeException {
    private int retCode;
    private String message;
    private MessageUtils messageUtils= SpringContextHolder.getBean(MessageUtils.class);

    public AbstractCodeException(int retCode){
        this.retCode=retCode;
    }
    public AbstractCodeException(int retCode,String message){
        this.retCode=retCode;
        this.message=message;
    }
    public AbstractCodeException(Exception ex){
        this.retCode=500;
        this.message=ex.getMessage();
    }

    public int getRetCode() {
        return retCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
