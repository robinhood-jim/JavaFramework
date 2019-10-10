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

public abstract class BaseDataBaseExportor {
	protected String userPath;
	protected String tmpPath;
	public BaseDataBaseExportor(){
		
	}
	protected void init(){
		tmpPath=System.getProperty("java.io.tmpdir");
		userPath=System.getProperty("user.dir");
	}
	public abstract int exportToLocal(DataBaseExportParam exParam,DataBaseParam param);
	public static String getSplit(String split){
		String retsplit="";
		if(split.startsWith("0x")){
			char splitbyte=(char) Integer.parseInt(split.substring(2));
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
