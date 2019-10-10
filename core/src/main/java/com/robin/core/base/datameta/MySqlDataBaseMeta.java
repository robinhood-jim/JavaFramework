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

public class MySqlDataBaseMeta extends BaseDataBaseMeta implements DataBaseInterface {

	public MySqlDataBaseMeta(DataBaseParam param) {
		super(param);
		if(param.getMainVersion()!=null && param.getMainVersion()!=0){
			if(param.getMainVersion()==5){
				param.setDriverClassName("com.mysql.jdbc.Driver");
			}else if(param.getMainVersion()>=8){
				param.setDriverClassName("com.mysql.cj.jdbc.Driver");
			}else {
				param.setDriverClassName("org.gjt.mm.mysql.Driver");
			}
		}else
			param.setDriverClassName("com.mysql.jdbc.Driver");
		if(param.getEncode()==null || !"".equals(param.getEncode().trim())){
			param.setEncode("utf-8");
		}
	}

	

	public String getUrlTemplate() {
		if(param.getMainVersion()!=null && param.getMainVersion()!=0){
			if(param.getMainVersion()<8){
				return "jdbc:mysql://[hostName]:[port]/[databaseName]?useUnicode=true&characterEncoding=[encode]&zeroDateTimeBehavior=convertToNull&serverTimezone=[timeZone]";
			}else{
				return "jdbc:mysql://[hostName]:[port]/[databaseName]?useUnicode=true&characterEncoding=[encode]&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=[timeZone]";
			}
		}else
			return "jdbc:mysql://[hostName]:[port]/[databaseName]?useUnicode=true&characterEncoding=[encode]&zeroDateTimeBehavior=convertToNull&serverTimezone=[timeZone]";
	}
	public boolean suppportSequnce() {
		return false;
	}

	public boolean supportAutoInc() {
		return true;
	}

	public int getDefaultDatabasePort() {
		return 3306;
	}

	

	public boolean supportsSchemas() {
		return true;
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
	@Override
	public String getCatalog(String schema) {
		return schema;
	}
}
