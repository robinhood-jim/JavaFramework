package com.robin.core.dbimp;

import java.util.ArrayList;
import java.util.List;

import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;

public class DB2DataBaseImportor extends BaseDataBaseImportor {

	
	public DB2DataBaseImportor(){
		super.init();
	}

	public int importFromLocal(DataBaseImportParam inparam,DataBaseParam param) {
		List<String> scriptList=new ArrayList<String>();
		scriptList.add(userPath+Const.DB2EXECUTE_SCRIPTS);
		scriptList.add(Const.DBIMP_SHELL_PARAM);
		scriptList.add(param.getHostName());
		scriptList.add(param.getUserName());
		scriptList.add(param.getPasswd());
		scriptList.add(inparam.getFilePath());
		scriptList.add(inparam.getTableName());
		scriptList.add(getSplit(inparam.getSplit()));
		
		ProcessBuilder builder=new ProcessBuilder(scriptList);
		int ret=-1;
		try{
			Process process=builder.start();
			ret=process.waitFor();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
		
	}

}
