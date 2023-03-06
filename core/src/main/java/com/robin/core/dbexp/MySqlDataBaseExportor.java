/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.dbexp;

import java.util.ArrayList;
import java.util.List;

import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.shell.CommandLineExecutor;

public class MySqlDataBaseExportor extends BaseDataBaseExportor {
	private String split=",";
	public MySqlDataBaseExportor(){
		
	}
	
	@Override
	public int exportToLocal(DataBaseExportParam exParam,DataBaseParam param) {
		if(exParam.getSplit()!=null){
			split=exParam.getSplit();
		}
		String script="mysql -u "+param.getUserName()+" -p"+param.getPasswd()+" -h "+param.getHostName()+" --database "+param.getDatabaseName()+" -N -s -r -e \""+exParam.getExecuteSql()+"\" | tr '\\t' '"+split+"' >"+exParam.getDumpPath();

		System.out.println(script);
		int ret=-1;
		try{
			List<String> cmdList=new ArrayList<>();
			cmdList.add("sh");
			cmdList.add("-c");
			cmdList.add(script);
			CommandLineExecutor.getInstance().executeCmd(cmdList);
			ret=0;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}
}
