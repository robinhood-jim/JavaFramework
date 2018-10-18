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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseDataBaseMeta implements DataBaseInterface {
	//static fields
	public static final String TYPE_MYSQL="MySql";
	public static final String TYPE_ORACLE="Oracle";
	public static final String TYPE_DB2="DB2";
	public static final String TYPE_SYBASE="Sybase";
	public static final String TYPE_SQLSERVER="SqlServer";
	public static final String TYPE_H2="H2";
	public static final String TYPE_DEBRY="Debry";
	public static final String TYPE_PGSQL="PostgreSql";
	public static final String TYPE_PHONEIX="Phoenix4";
	public static final String TYPE_HIVE="Hive";
	public static final String TYPE_HIVE2="Hive2";
	public static final String TYPE_Impala="Impala";
	protected DataBaseParam param;
	//Enum type of all support DB
	public static String[] dbTypeEnmu={"Oracle","MySql","DB2","SqlServer","Sybase","H2","Debry","PostgreSql","Phoenix","Hive","Hive2","OracleRac"};
	//jdbc Url Template like jdbc:mysql://[hostName]:[port]/[databaseName]?useUnicode=true&characterEncoding=[encode]
	protected static final Pattern PATTERN_TEMPLATE_PARAM = Pattern.compile("\\[.*?\\]");
	public List<DataBaseTableMeta> listAllTable(String schema) throws Exception {
		DataBaseUtil util=new DataBaseUtil();
		util.connect(this,param);
		List<DataBaseTableMeta> list=util.getAllTable(schema, this);
		util.closeConnection();
		return list;
	}
	public String getUrl(DataBaseParam param) throws Exception{
		try{
			if(param.getUrl()==null){
				if(param.getPort()==0)
					param.setPort(getDefaultDatabasePort());
				Matcher matcher=PATTERN_TEMPLATE_PARAM.matcher(param.getUrlTemplate());
				Map<String, String> map=new HashMap<String, String>();
				StringBuffer builder=new StringBuffer();
				processParam(map);
				while(matcher.find()){
					String word=matcher.group();
					String v_word = word.replaceFirst("\\[", "");
					v_word = v_word.replaceFirst("\\]", "");
					matcher.appendReplacement(builder, map.get(v_word));
				}
				matcher.appendTail(builder);
				param.setUrl(builder.toString());
			}
		}catch(Exception ex){
			throw ex;
		}
		return param.getUrl();
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
	public String getSQLNextSequenceValue(String sequenceName) {
		return null;
	}

	public String getSQLCurrentSequenceValue(String sequenceName) {
		return null;
	}
	public DataBaseParam getParam(){
		return this.param;
	}

	public String getSQLSequenceExists(String sequenceName) {
		return null;
	}
	public abstract BaseSqlGen getSqlGen();
	@Override
	public boolean equals(Object obj) {
		boolean isequal=false;
		if(obj instanceof BaseDataBaseMeta){
			BaseDataBaseMeta compareObj=(BaseDataBaseMeta) obj;
			if(this.getDriverClass().equals(compareObj.getDriverClass()) && this.getParam().equals(compareObj.getParam())){
				isequal=true;
			}
		}
		return isequal;
	}

}
