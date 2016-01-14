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

import com.robin.core.base.datameta.DataBaseParam;

public class HiveDataBaseExporter extends BaseDataBaseExportor {
	private String scriptPath;
	public HiveDataBaseExporter(String scriptPath){
		this.scriptPath=scriptPath;
	}
	@Override
	public int exportToLocal(DataBaseExportParam exParam,DataBaseParam param) {
		String[] cmdArr={"sh","-c","hive","INSERT OVERWRITE DIRECTORY",exParam.getDumpPath(),exParam.getExecuteSql()};
		ProcessBuilder builder=new ProcessBuilder(cmdArr);
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
