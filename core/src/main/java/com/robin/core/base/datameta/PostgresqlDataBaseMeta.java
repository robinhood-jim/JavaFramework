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
import com.robin.core.sql.util.PostgreSqlSqlGen;

public class PostgresqlDataBaseMeta extends BaseDataBaseMeta{

	public PostgresqlDataBaseMeta(DataBaseParam param) {
		super(param);
		setDbType(BaseDataBaseMeta.TYPE_PGSQL);
		param.setDriverClassName("org.postgresql.Driver");
	}

	@Override
    public String getUrlTemplate() {
		return "jdbc:postgresql://[hostName]:[port]/[databaseName]";
	}


	@Override
    public boolean suppportSequnce() {
		return false;
	}

	@Override
    public boolean supportAutoInc() {
		return false;
	}

	@Override
    public int getDefaultDatabasePort() {
		return 5432;
	}

	@Override
    public boolean supportsSchemas() {
		return false;
	}

	@Override
    public BaseSqlGen getSqlGen() {
		return PostgreSqlSqlGen.getInstance();
	}

}
