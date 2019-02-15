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

import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.MysqlSqlGen;

public class HiveDataBaseMeta extends BaseDataBaseMeta{

	public HiveDataBaseMeta(DataBaseParam param) {
		super(param);
		if(param.getDatabaseName()==null || param.getDatabaseName().equals("")){
			param.setDatabaseName("default");
		}
		param.setDriverClassName("org.apache.hadoop.hive.jdbc.HiveDriver");
	}

	public String getUrlTemplate() {
		return "jdbc:hive://[hostName]:[port]/[databaseName]";
	}

	public boolean suppportSequnce() {
		return false;
	}

	public boolean supportAutoInc() {
		return false;
	}

	public int getDefaultDatabasePort() {
		return 10000;
	}

	public boolean supportsSchemas() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getAddColumnStatement(String tablename, String schema,
			DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
			boolean semicolon) {
		
		return null;
	}

	public String getDropColumnStatement(String tablename, String schema,
			DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
			boolean semicolon) {
		
		return null;
	}
	public BaseSqlGen getSqlGen() {
		return new MysqlSqlGen();
	}
}
