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

public class PhoenixDataBaseMeta extends BaseDataBaseMeta {

	public PhoenixDataBaseMeta(DataBaseParam param) {
		super(param);
		param.setDriverClassName("org.apache.phoenix.queryserver.client.Driver");
	}


	public String getAddColumnStatement(String arg0, String arg1,
			DataBaseColumnMeta arg2, String arg3, boolean arg4, String arg5,
			boolean arg6) {
		
		return null;
	}

	public int getDefaultDatabasePort() {
		return 2181;
	}
	

	public String getDropColumnStatement(String arg0, String arg1,
			DataBaseColumnMeta arg2, String arg3, boolean arg4, String arg5,
			boolean arg6) {
		return null;
	}

	public String getUrlTemplate() {
		return "jdbc:phoenix:thin:url=[hostName]:[port]";
	}


	public boolean supportAutoInc() {
		return false;
	}

	public boolean supportsSchemas() {
		return false;
	}

	public boolean suppportSequnce() {
		return false;
	}
	public BaseSqlGen getSqlGen() {
		return new MysqlSqlGen();
	}

}
