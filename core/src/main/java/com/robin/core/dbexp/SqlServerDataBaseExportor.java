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

public class SqlServerDataBaseExportor extends BaseDataBaseExportor{
	//"bcp \""+exParam.getExecuteSql()+"\"  queryout  "+exParam.getDumpPath()+" -t "+exParam.getSplit()+" -S "+param.getHostName()+" -U "+param.getUserName()+" -P "+param.getPasswd()+" -T -c";
	@Override
	public int exportToLocal(DataBaseExportParam exParam,
			DataBaseParam param) {
		List<String> scriptList=new ArrayList<>();
		scriptList.add("bcp");
		scriptList.add(exParam.getExecuteSql());
		scriptList.add("queryout");
		scriptList.add(exParam.getDumpPath());
		scriptList.add("-t");
		scriptList.add(exParam.getSplit());
		scriptList.add("-S");
		scriptList.add(param.getHostName());
		scriptList.add("-U");
		scriptList.add(param.getUserName());
		scriptList.add("-P");
		scriptList.add(param.getPasswd());
		scriptList.add("-T");
		scriptList.add("-c");

		int ret=-1;
		try{
			CommandLineExecutor.getInstance().executeCmd(scriptList);
			ret=0;
		}catch(Exception ex){

		}
		return ret;
	}

}
