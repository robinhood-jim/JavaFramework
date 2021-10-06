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

import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
@Slf4j
public abstract class BaseDataBaseMeta implements DataBaseInterface, Serializable {
	//static fields
	public static final String TYPE_MYSQL="Mysql";
	public static final String TYPE_ORACLE="Oracle";
	public static final String TYPE_ORACLERAC="Oraclerac";
	public static final String TYPE_DB2="DB2";
	public static final String TYPE_SYBASE="Sybase";
	public static final String TYPE_SQLSERVER="Sqlserver";
	public static final String TYPE_H2="H2";
	public static final String TYPE_DEBRY="Debry";
	public static final String TYPE_PGSQL="Postgresql";
	public static final String TYPE_PHONEIX="Phoenix4";
	public static final String TYPE_HIVE="Hive1";
	public static final String TYPE_HIVE2="Hive";
	public static final String TYPE_IMPALA ="Impala";
	protected DataBaseParam param;
	protected String dbType;
	//Enum type of all support DB
	public static final String[] DB_TYPE_ENMU ={"Oracle","Mysql","DB2","Sqlserver","Sybase","Postgresql","Phoenix4","Hive1","Hive","Oraclerac","H2","Impala"};
	//jdbc Url Template like jdbc:mysql://[hostName]:[port]/[databaseName]?useUnicode=true&characterEncoding=[encode]
	public static final Pattern PATTERN_TEMPLATE_PARAM = Pattern.compile("\\[.*?\\]");
	@Override
	public List<DataBaseTableMeta> listAllTable(String schema) throws Exception {
		DataBaseUtil util=new DataBaseUtil();
		util.connect(this);
		List<DataBaseTableMeta> list=util.getAllTable(schema);
		util.closeConnection();
		return list;
	}
	@Override
	public String getUrl(){

		return param.getUrlByMeta(this);
	}
	protected void processParam(Map<String,String> map) throws Exception{
		ConvertUtil.objectToMap(map, param);
	}
	
	public BaseDataBaseMeta(DataBaseParam param){
		if(param!=null){
			this.param=param;
			if(param.getUrlTemplate()==null || param.getUrlTemplate().isEmpty()){
				param.setUrlTemplate(getUrlTemplate());
			}
		}
	}
	@Override
	public String getSQLNextSequenceValue(String sequenceName) {
		return null;
	}

	@Override
	public String getSQLCurrentSequenceValue(String sequenceName) {
		return null;
	}
	@Override
	public DataBaseParam getParam(){
		return this.param;
	}

	@Override
	public String getSQLSequenceExists(String sequenceName) {
		return null;
	}
	public abstract BaseSqlGen getSqlGen();
	@Override
	public boolean equals(Object obj) {
		boolean isequal=false;
		if(obj instanceof BaseDataBaseMeta){
			BaseDataBaseMeta compareObj=(BaseDataBaseMeta) obj;
			if(param.getDriverClassName().equals(param.getDriverClassName()) && this.getParam().equals(compareObj.getParam())){
				isequal=true;
			}
		}
		return isequal;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String getCatalog(String schema) {
		return null;
	}
	public String getDbType(){
		return dbType;
	}
	public void setDbType(String dbType){
		this.dbType=dbType;
	}
	public String getCreateExtension(){
		return "";
	}

	@Override
	public String getAddColumnStatement(String tableName, String schema,
										DataBaseColumnMeta v) {
		StringBuilder builder=new StringBuilder();
		builder.append("ALTER TABLE ").append(getTableSpec(tableName,schema)).append(" ADD COLUMN ");
		builder.append(v.getColumnName()).append(" ").append(getSqlGen().returnTypeDef(v.getColumnType().toString(),v));
		if(v.isIncrement() && supportAutoInc()){
			builder.append(" ").append(getSqlGen().getAutoIncrementDef());
		}
		if(v.isPrimaryKey()){
			builder.append(" PRIMARY KEY");
		}
		return builder.toString();
	}
	private String getTableSpec(String tableName,String schema){
		Assert.notNull(tableName,"");
		StringBuilder builder=new StringBuilder();
		if(!StringUtils.isEmpty(schema)){
			builder.append(schema).append(".");
		}
		builder.append(tableName);
		return builder.toString();
	}

	@Override
	public String getDropColumnStatement(String tableName, String schema,
										 DataBaseColumnMeta v) {
		StringBuilder builder=new StringBuilder();
		builder.append("ALTER TABLE ").append(getTableSpec(tableName,schema)).append(" DROP COLUMN ");
		builder.append(v.getColumnName());
		return builder.toString();
	}
}
