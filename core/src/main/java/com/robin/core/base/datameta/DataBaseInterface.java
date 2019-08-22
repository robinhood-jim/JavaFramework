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
	String getUrl() throws Exception;
	boolean suppportSequnce();
	boolean supportAutoInc();
	int getDefaultDatabasePort();
	String getSQLNextSequenceValue(String sequenceName);
	String getSQLCurrentSequenceValue(String sequenceName);
	String getSQLSequenceExists(String sequenceName);
	boolean supportsSchemas();
	List<DataBaseTableMeta> listAllTable(String schema) throws Exception;
	String getAddColumnStatement(String tablename, String schema,DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
	String getDropColumnStatement(String tablename,String schema, DataBaseColumnMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
	DataBaseParam getParam();
	String getUrlTemplate();
}
