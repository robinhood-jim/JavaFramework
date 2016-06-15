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

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

public class SplitPageResultSetExtractor implements ResultSetExtractor<List<Map<String,Object>>> {
	private final int start;

	private final int len;
    private LobHandler lobHandler;
    private List<Map<String,Object>> mappingFieldList;

	public SplitPageResultSetExtractor(int start, int len) {
		this.start = start;
		this.len = len;
	}
	public SplitPageResultSetExtractor(int start, int len,LobHandler handler,List<Map<String,Object>> mappingFieldList) {
		
		this.start = start;
		this.len = len;
		this.lobHandler=handler;
		this.mappingFieldList=mappingFieldList;
	}
	public SplitPageResultSetExtractor(int start, int len,LobHandler handler) {
		
		this.start = start;
		this.len = len;
		this.lobHandler=handler;
	}

	@Override
	public List<Map<String,Object>> extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		return wrapMapper(rs, start, len);
	}
	public List<Map<String,Object>> wrapMapper(ResultSet rs,int start,int len) throws SQLException,DataAccessException {
		int end = start + len;
		boolean allcode=false;
		if(end==0)
			allcode=true;
		int rowNum = 0;
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();
			String[] columnName=new String[count];
			String[] typeName=new String[count];
			String[] className=new String[count];
			for(int k=0;k<count;k++){
				if(mappingFieldList!=null && !mappingFieldList.isEmpty()){
					columnName[k]=mappingFieldList.get(k).get("name").toString();
				}else
					columnName[k] = rsmd.getColumnLabel(k + 1);
				typeName[k]=rsmd.getColumnTypeName(k+1);
				String fullclassName=rsmd.getColumnClassName(k+1);
				int pos=fullclassName.lastIndexOf(".");
				className[k]=fullclassName.substring(pos+1,fullclassName.length()).toUpperCase();
			}
			while (rs.next()){
				++rowNum;
				if(!allcode){
					if (rowNum <= start) 
						continue ;
					else if (rowNum > end) 
						break ;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < count; i++) {
					Object obj=rs.getObject(i+1);
					if(rs.wasNull())
					{
						map.put(columnName[i], "");
					}else if(typeName[i].equalsIgnoreCase("DATE"))
					{
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
						Date date=rs.getDate(i+1);
						String datestr=format.format(date);
						map.put(columnName[i], datestr);
					}else if(typeName[i].equalsIgnoreCase("TIMESTAMP"))
					{
						SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Timestamp stamp=rs.getTimestamp(i+1);
						String datestr=format.format(new Date(stamp.getTime()));
						map.put(columnName[i], datestr);
					}else if(className[i].toUpperCase().contains("CLOB")){
						if(lobHandler!=null){
							String result=lobHandler.getClobAsString(rs, i+1);
							map.put(columnName[i], result);
						}
					}
					else if(className[i].toUpperCase().contains("BLOB") || typeName[i].toUpperCase().contains("BLOB")){
						if(lobHandler!=null){
							byte[] bytes=lobHandler.getBlobAsBytes(rs, i+1);
							map.put(columnName[i], bytes);
						}
					}
					else
						map.put(columnName[i], rs.getObject(i + 1).toString().trim());
				}
				list.add(map);
			}
		return list;
	}
	public Map<String, Object> wrapResultRecord(int count,ResultSet rs,String[] columnName,String[] typeName,String[] className) throws SQLException{
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < count; i++) {
			Object obj=rs.getObject(i+1);
			if(rs.wasNull())
			{
				map.put(columnName[i], "");
			}else if(typeName[i].equalsIgnoreCase("DATE"))
			{
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
				Date date=rs.getDate(i+1);
				String datestr=format.format(date);
				map.put(columnName[i], datestr);
			}else if(typeName[i].equalsIgnoreCase("TIMESTAMP"))
			{
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Timestamp stamp=rs.getTimestamp(i+1);
				String datestr=format.format(new Date(stamp.getTime()));
				map.put(columnName[i], datestr);
			}else if(className[i].contains("CLOB")){
				if(lobHandler!=null){
					String result=lobHandler.getClobAsString(rs, i+1);
					map.put(columnName[i], result);
				}
			}
			else if(className[i].contains("BLOB")){
				if(lobHandler!=null){
					byte[] bytes=lobHandler.getBlobAsBytes(rs, i+1);
					map.put(columnName[i], new String(bytes));
				}
			}
			else
				map.put(columnName[i], rs.getObject(i + 1).toString().trim());
		}
		return map;
	}
	
}
