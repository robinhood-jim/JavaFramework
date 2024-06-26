package com.robin.hadoop.hdfs;

public class HdfsException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public HdfsException(String message){
		super(message);
	}
	public HdfsException(Exception ex){
		super(ex);
	}
}
