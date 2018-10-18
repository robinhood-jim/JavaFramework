package com.robin.core.dbimp;

import com.robin.core.base.datameta.DataBaseParam;

public abstract class BaseDataBaseImportor {
	protected String userPath;
	protected String tmpPath;
	public BaseDataBaseImportor(){
		
	}
	protected void init(){
		tmpPath=System.getProperty("java.io.tmpdir");
		userPath=System.getProperty("user.dir");
	}
	public abstract int importFromLocal(DataBaseImportParam inparam,DataBaseParam param);
	public static String getSplit(String split){
		String retsplit="";
		if(split.startsWith("0x")){
			char splitbyte=(char) Integer.parseInt(split.substring(2,split.length()));
			char[] chars={splitbyte};
			retsplit=new String(chars);
		}else{
			if(split.equals("|")|| split.equals("\"")||split.equals("\\")){
				retsplit="\\"+split;
			}
		}
		return retsplit;
	}
}
