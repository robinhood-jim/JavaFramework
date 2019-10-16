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
import com.robin.core.sql.util.SqlServer2005Gen;

public class SqlServerDataBaseMeta extends BaseDataBaseMeta {

	public SqlServerDataBaseMeta(DataBaseParam param) {
		super(param);
		setDbType(BaseDataBaseMeta.TYPE_SQLSERVER);
		param.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	}


	@Override
    public String getUrlTemplate() {
		return "jdbc:sqlserver://[hostName]:[port];DatabaseName=[databaseName]";
	}

	@Override
    public boolean suppportSequnce() {
		return false;
	}

	@Override
    public boolean supportAutoInc() {
		return true;
	}

	@Override
    public int getDefaultDatabasePort() {
		return 1433;
	}

	@Override
    public boolean supportsSchemas() {
		return false;
	}

	@Override
    public String getAddColumnStatement(String tablename, String schema,
                                        DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
                                        boolean semicolon) {
		return null;
	}

	@Override
    public String getDropColumnStatement(String tablename, String schema,
                                         DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk,
                                         boolean semicolon) {
		
		return null;
	}
	@Override
    public BaseSqlGen getSqlGen() {
		return new SqlServer2005Gen();
	}
	@Override
	public String getCatalog(String schema) {
		return schema;
	}
}
