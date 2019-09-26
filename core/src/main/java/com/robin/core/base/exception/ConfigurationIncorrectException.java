package com.robin.core.base.exception;

/**
 * <p>Created at: 2019-09-26 11:16:53</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ConfigurationIncorrectException extends RuntimeException{
    public ConfigurationIncorrectException(RuntimeException ex) {
        super(ex);
    }
    public ConfigurationIncorrectException(String description) {
        super(description);
    }
}
