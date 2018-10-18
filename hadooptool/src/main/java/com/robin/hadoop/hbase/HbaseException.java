package com.robin.hadoop.hbase;

@SuppressWarnings("serial")
public class HbaseException extends Exception {
	public HbaseException(String message){
		super(message);
	}
	public HbaseException(Exception ex){
		super(ex);
	}

}
