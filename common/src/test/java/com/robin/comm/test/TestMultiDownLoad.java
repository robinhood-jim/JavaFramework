package com.robin.comm.test;

import com.robin.comm.ftp.MultiThreadFtp;

public class TestMultiDownLoad {
	public static void main(String[] args){
		MultiThreadFtp ftp=new MultiThreadFtp("localhost", "root", "root");
		try{
			ftp.downLoadLargeFile(args[0], args[1], Integer.valueOf(args[2]), 3);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}

