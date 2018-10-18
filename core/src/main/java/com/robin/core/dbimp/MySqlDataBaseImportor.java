package com.robin.core.dbimp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.robin.core.base.datameta.DataBaseParam;


public class MySqlDataBaseImportor extends BaseDataBaseImportor {

	public int importFromLocal(DataBaseImportParam inparam,DataBaseParam param) {
		String[] scriptArr={"mysql -u ",param.getUserName(),"-p"+param.getPasswd(),"-h ",param.getHostName(),"--database ",param.getDatabaseName()};
		int ret=-1;
		try{
			List<String> cmdList=new ArrayList<String>();
			cmdList.addAll(Arrays.asList(scriptArr));
			cmdList.add("load data local infile");
			cmdList.add(inparam.getFilePath());
			cmdList.add("INTO TABLE "+inparam.getTableName());
			cmdList.add("COLUMNS");
			ProcessBuilder builder=new ProcessBuilder(cmdList);
			Process process=builder.start();
			ret=process.waitFor();
			System.out.println(ret);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
		
	}
	

}
