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
package com.robin.core.base.dao;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.object.StoredProcedure;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseStoreProcedure extends StoredProcedure{
	private Map<String,Object> initParameterData = new HashMap<>();
    private Map<String,?> inParam;                             
    
	public BaseStoreProcedure(DataSource dataSource,String sql){
		super(dataSource,sql);
		this.compile();
	}
	public BaseStoreProcedure(JdbcTemplate jdbcTemplate,String sql){
		setJdbcTemplate(jdbcTemplate); 
        setSql(sql); 
	}
	protected BaseStoreProcedure(JdbcTemplate jdbcTemplate, String sql,List<?> declareParameterList){
		 setJdbcTemplate(jdbcTemplate);
         setSql(sql);
        for (Object o : declareParameterList) {
            SqlParameter parameter = (SqlParameter) o;
            this.declareParameter(parameter);
        }
        this.compile();
	}

	private final RowMapper<?> rm = (RowMapper<Object>) (rs, rowNum) -> {
         int count = rs.getMetaData().getColumnCount();
         String[] header = new String[count];
         for(int i=0;i<count;i++) {
             header[i] = rs.getMetaData().getColumnName(i+1);
         }
         HashMap<String,String> row = new HashMap<>(); //count+7
         for(int i=0;i<count;i++){
             row.put(header[i],rs.getString(i+1));
         }
        return row;
    };

    //set Output Paramter
    public void setOutParameter(String column,int type){ 
        declareParameter(new SqlOutParameter(column, type));
    } 
    //set InOutput Paramter
    public void setInOutParameter(String column,int type){ 
        declareParameter(new SqlInOutParameter(column, type));
    } 
    public void setReturnResultSet(String column,SqlReturnResultSet rset){
    	if(rset==null) {
            rset=new SqlReturnResultSet(column,rm);
        }
    	declareParameter(rset);
    }

    //Set parameter with Type
    public void setParameter(String column,int type){ 
        declareParameter(new SqlParameter(column, type)); 
    } 
  //set In Paramter
    public void setInParam(Map<String,Object> inParam){
        this.inParam = inParam; 
    } 

    public Map<String,Object> execute() { 
        compile(); 
        return execute(this.inParam); 
    } 
    public Map<String,Object> getInitParameterData() { 
        return initParameterData; 
    } 

    public void setInitParameterData(Map<String,Object> initParameterData) { 
        this.initParameterData = initParameterData; 
    } 

    public void setCallFunction(boolean callFunction) { 
        this.setFunction(callFunction); 
    } 


}
