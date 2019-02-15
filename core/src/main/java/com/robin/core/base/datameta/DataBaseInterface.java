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

import java.util.List;

public interface DataBaseInterface {
	//public String getDriverClass();
	public String getUrl(DataBaseParam param) throws Exception;
	public boolean suppportSequnce();
	public boolean supportAutoInc();
	public int getDefaultDatabasePort();
	public String getSQLNextSequenceValue(String sequenceName);
	public String getSQLCurrentSequenceValue(String sequenceName);
	public String getSQLSequenceExists(String sequenceName);
	public boolean supportsSchemas();
	public List<DataBaseTableMeta> listAllTable(String schema) throws Exception;
	public String getAddColumnStatement(String tablename, String schema,DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
	public String getDropColumnStatement(String tablename,String schema, DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
	public DataBaseParam getParam();
	public String getUrlTemplate();
}
