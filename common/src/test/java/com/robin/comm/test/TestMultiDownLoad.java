package com.robin.comm.test;

import com.robin.comm.ftp.MultiThreadFtp;

/**
 * <p>Title: com.robin.test</p>
 *
 * <p>Description:TestMultiDownLoad.java</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
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

