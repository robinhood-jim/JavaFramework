package com.robin.core.base.exception;

/**
 * <p>Created at: 2019-08-21 17:03:07</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class MissingConfigException extends RuntimeException {
    public MissingConfigException(RuntimeException ex) {
        super(ex);
    }
    public MissingConfigException(String message) {
        super(message);
    }
}
