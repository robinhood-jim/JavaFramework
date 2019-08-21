package com.robin.core.base.exception;

/**
 * <p>Created at: 2019-08-21 17:03:07</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class MissingConfigExecption extends RuntimeException {
    public MissingConfigExecption(RuntimeException ex) {
        super(ex);
    }
    public MissingConfigExecption(String message) {
        super(message);
    }
}
