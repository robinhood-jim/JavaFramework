package com.robin.core.dbimp;

import java.util.ArrayList;
import java.util.List;

import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.shell.CommandLineExecutor;


public class SqlServerDataImportor  extends BaseDataBaseImportor{
	@Override
	public int importFromLocal(DataBaseImportParam inparam, DataBaseParam param) {
		List<String> scriptList=new ArrayList<String>();
		scriptList.add("bcp");
		scriptList.add(param.getDatabaseName()+"."+inparam.getSchema()+"."+inparam.getTableName());
		scriptList.add("in");
		scriptList.add(inparam.getFilePath());
		scriptList.add("-t");
		scriptList.add(getSplit(inparam.getSplit()));
		scriptList.add("-S");
		scriptList.add(param.getHostName());
		scriptList.add("-U");
		scriptList.add(param.getUserName());
		scriptList.add("-P");
		scriptList.add(param.getPasswd());
		scriptList.add("-T");
		scriptList.add("-c");
		int ret=0;
		try{
			CommandLineExecutor.getInstance().executeCmd(scriptList,System.currentTimeMillis());
		}catch(Exception ex){
			ret=-1;
		}
		return ret;
	}
}
