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
package com.robin.core.query.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class ResultSetOperationExtractor implements ResultSetExtractor<Integer> {
	private String dateFormat="yyyy-MM-dd";
	private String timestampFormat="yyyy-MM-dd HH:mm:ss";
	protected String encode="UTF-8";
	private LobHandler lobHandler;
	public ResultSetOperationExtractor(){
		init();
	}
	public ResultSetOperationExtractor(String dateFormat,String timestampFormat) {
		if(dateFormat!=null)
			this.dateFormat=dateFormat;
		if(timestampFormat!=null){
			this.timestampFormat=timestampFormat;
		}
	}
	public Integer extractData(ResultSet rs) throws SQLException,
			DataAccessException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] columnName=new String[count];
			String[] typeName=new String[count];
			String[] className=new String[count];
			Integer retVal=0;
			for(int k=0;k<count;k++){
				columnName[k] = rsmd.getColumnLabel(k + 1);
				typeName[k]=rsmd.getColumnTypeName(k+1);
				try{
					String fullclassName=rsmd.getColumnClassName(k+1);
					int pos=fullclassName.lastIndexOf(".");
					className[k]=fullclassName.substring(pos+1,fullclassName.length()).toUpperCase();
				}catch(Exception ex){
					//logger.warn("--------getColumnClassName not support....");
				}
				if(className[k]==null){
					className[k]="java.lang.String";
				}
			}
			Object obj=null;
			Date date=null;
			Timestamp stamp=null;
			String result=null;
			while (rs.next()){
 				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < count; i++) {
					obj=rs.getObject(i+1);
					if(rs.wasNull())
					{
						map.put(columnName[i], "");
					}else if(typeName[i].equalsIgnoreCase("DATE"))
					{
						//SimpleDateFormat format=new SimpleDateFormat(dateFormat);
						date=rs.getDate(i+1);
						//String datestr=format.format(date);
						map.put(columnName[i], date);
					}else if(typeName[i].equalsIgnoreCase("TIMESTAMP") || typeName[i].equalsIgnoreCase("datetime"))
					{
						//SimpleDateFormat format=new SimpleDateFormat(timestampFormat);
						stamp=rs.getTimestamp(i+1);
						//String datestr=format.format(new Date(stamp.getTime()));
						map.put(columnName[i], stamp);
					}else if(className[i].contains("CLOB")){
						try {
							result = new String(rs.getBytes(i + 1), encode);
							map.put(columnName[i], result);
						}catch (UnsupportedEncodingException ex){
							throw new SQLException(ex);
						}
					}else if(className[i].contains("BLOB") || className[i].equals("OBJECT")){
						obj=rs.getBytes(i+1);
						map.put(columnName[i], obj);
					}
					else
						map.put(columnName[i], obj);
				}
				if(executeAddtionalOperation(map, columnName, typeName, className)){
					retVal++;
				}
		}
		return retVal;
	}
	public void setEncode(String encode){
		this.encode=encode;
	}
	
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getTimestampFormat() {
		return timestampFormat;
	}
	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	public abstract boolean executeAddtionalOperation(Map<String,Object> map,String[] columnName,String[] typeName,String[] className) throws SQLException;
	public abstract void init();
}
