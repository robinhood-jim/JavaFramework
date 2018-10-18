package com.robin.core.base.exception;
/**
 * <p>Project:  core</p>
 *
 * <p>Description:ResourceNotAvailableException.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月25日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ResourceNotAvailableException extends RuntimeException{
	public ResourceNotAvailableException(Throwable ex) {
		super(ex);
	}
	public ResourceNotAvailableException(String errMsg) {
		super(errMsg);
	}
}
