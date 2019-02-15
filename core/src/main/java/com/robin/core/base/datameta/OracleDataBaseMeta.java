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
package com.robin.core.base.datameta;

import java.util.Map;

import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.OracleSqlGen;

public class OracleDataBaseMeta extends BaseDataBaseMeta implements DataBaseInterface{
	public static String ORA_TYPE_NORMAL="0";
	public static String ORA_TYPE_DATABASE="1";
	public static String ORA_TYPE_CLUSTER="2";
	public OracleDataBaseMeta(DataBaseParam param) {
		super(param);
		param.setDriverClassName("oracle.jdbc.driver.OracleDriver");
	}


	public String getUrlTemplate() {
		String ret="";
		if(param.getType()==null){
			ret= "jdbc:oracle:thin:@[hostName]:[port]:[databaseName]";
		}else if(param.getType().equals(ORA_TYPE_DATABASE)){
			ret= "jdbc:oracle:thin:@[hostName]:[port]/[databaseName]";
		}else if(param.getType().equals(ORA_TYPE_CLUSTER)){
			ret= "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=[ip1])(PORT=[port]))(ADDRESS=(PROTOCOL=TCP)(HOST=[ip2])(PORT=[port])))(LOAD_BALANCE=yes)(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=[databaseName])))";
		}
		return ret;
	}
	protected void processParam(Map<String, String> map) throws Exception {
		super.processParam(map);
		if(param.getType()!=null && param.getType().equals(ORA_TYPE_CLUSTER)){
			String[] ipAdd=param.getHostName().split(";");
			for (int i = 0; i < ipAdd.length; i++) {
				map.put("ip"+i, ipAdd[i]);
			}
			
		}
	}

	public boolean suppportSequnce() {
		return true;
	}

	public boolean supportAutoInc() {
		return true;
	}

	public int getDefaultDatabasePort() {
		return 1521;
	}

	public boolean supportsSchemas() {
		return false;
	}

	public String getAddColumnStatement(String tablename, String schema,DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
			boolean semicolon) {
		return null;
	}

	public String getDropColumnStatement(String tablename, String schema,DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
			boolean semicolon) {
		
		return null;
	}
	public BaseSqlGen getSqlGen() {
		return new OracleSqlGen();
	}
}
