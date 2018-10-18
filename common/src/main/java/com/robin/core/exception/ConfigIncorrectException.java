package com.robin.core.exception;

@SuppressWarnings("serial")
public class ConfigIncorrectException extends RuntimeException{
	public ConfigIncorrectException(RuntimeException ex) {
		super(ex);
	}
	public ConfigIncorrectException(String description) {
		super(description);
	}

}
