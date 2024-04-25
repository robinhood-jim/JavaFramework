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
package com.robin.core.query.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PageQuery implements Serializable {
	protected String								groupByString;

	protected String								orderString;

	protected String								havingString;

	protected String								whereString;

	protected Integer							pageSize;

	protected Integer							pageNumber;

	protected Integer							recordCount;

	protected Integer							pageCount;

	protected String								selectParamId;

	protected String							order;				

	protected String							orderDirection;	

	private Map<String, String>		parameters=new HashMap<>();
	private Map<String,Object> namedParameters =new HashMap<>();
	private Object[] 	parameterArr;
	private Map<String, String>			columnTypes;

	private List<Condition>				conditions;

	private List<Map<String, Object>>	recordSet;
	
	protected String 					querySql;			


	protected String                         pageToolBar;
	protected String dateFormatString="yyyy-MM-dd";
	protected String timestampFormatString="yyyy-MM-dd HH:mm:ss";
	protected SimpleDateFormat dateFormater=null;
	protected SimpleDateFormat timestampFormater=null;

	public static final String						ASC	= "asc";

	public static final String						DESC	= "desc";


	public PageQuery() {
		pageNumber = 1;
		pageSize = 10;
	}
	public PageQuery(String order,String orderDirection){
		this.order=order;
		this.orderDirection=orderDirection;
	}
	public PageQuery(Integer pageSize,Integer pageNumber,String order,String orderDirection){
		this.order=order;
		this.orderDirection=orderDirection;
		this.pageSize=pageSize;
		this.pageNumber=pageNumber;
	}

	public void setParameterWithKey(String key,String value){
		this.parameters.put(key,value);
	}

	public void setNameParameterWithKey(String key,Object value){
		namedParameters.put(key,value);
	}

	public SimpleDateFormat getDateFormater(){
		if(dateFormater==null){
			dateFormater=new SimpleDateFormat(dateFormatString);
		}
		return dateFormater;
	}
	public SimpleDateFormat getTimestampFormater(){
		if(timestampFormater==null){
			timestampFormater=new SimpleDateFormat(timestampFormatString);
		}
		return timestampFormater;
	}
	public static class Builder{
		private PageQuery pageQuery=new PageQuery();
		public Builder setPageSize(Integer pageSize){
			pageQuery.setPageSize(pageSize);
			return this;
		}
		public Builder setPageCount(Integer pageCount){
			pageQuery.setPageCount(pageCount);
			return this;
		}
		public Builder setSelectedId(String selectedId){
			pageQuery.setSelectParamId(selectedId);
			return this;
		}
		public Builder setPageNumber(Integer pageNumber){
			pageQuery.setPageNumber(pageNumber);
			return this;
		}
		public Builder setOrder(String order){
			pageQuery.setOrder(order);
			return this;
		}
		public Builder setOrderDir(String orderDir){
			pageQuery.setOrderDirection(orderDir);
			return this;
		}
		public Builder setParameterArr(Object[] objects){
			pageQuery.setParameterArr(objects);
			return this;
		}
		public Builder putNamedParameter(String key,Object value){
			pageQuery.getNamedParameters().put(key,value);
			return this;
		}
		public Builder addParameter(String key,String value){
			pageQuery.getParameters().put(key,value);
			return this;
		}
		public Builder setNamedParameters(Map<String,Object> namedParameter){
			pageQuery.setNamedParameters(namedParameter);
			return this;
		}
		public Builder setParameters(Map<String,String> parameters){
			pageQuery.setParameters(parameters);
			return this;
		}
		public PageQuery build(){
			return pageQuery;
		}

	}
	

}
