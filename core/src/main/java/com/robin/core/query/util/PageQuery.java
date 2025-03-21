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

import com.robin.core.sql.util.FilterCondition;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class  PageQuery<T> implements Serializable {
	protected String								groupByString;

	protected String								orderString;

	protected String								havingString;

	protected String								whereString;

	protected Integer							pageSize;

	protected Integer 					   currentPage;

	protected Integer 						total;

	protected Integer 						pageCount;

	protected String							selectParamId;

	protected String							order;				

	protected String							orderDirection;	

	private Map<String, String>		parameters=new HashMap<>();
	private Map<String,Object> namedParameters =new HashMap<>();
	private List<Object> queryParameters=new ArrayList<>();
	private Map<String, String>			columnTypes;

	private List<Condition>				conditions;

	private List<T>	recordSet=new ArrayList<>();

	private Map<String, FilterCondition> conditionMap=new HashMap<>();

	public PageQuery() {
		currentPage = 1;
		pageSize = 10;
	}
	public PageQuery(String order, String orderDirection){
		this.order=order;
		this.orderDirection=orderDirection;
	}
	public PageQuery(Integer pageSize, Integer pageNumber, String order, String orderDirection){
		this.order=order;
		this.orderDirection=orderDirection;
		this.pageSize=pageSize;
		this.currentPage =pageNumber;
	}

	public void setParameterWithKey(String key,String value){
		this.parameters.put(key,value);
	}

	public void setNameParameterWithKey(String key,Object value){
		namedParameters.put(key,value);
	}



	public Map<String,Object> toMap(){
		Map<String,Object> retMap=new HashMap<>();
		retMap.put("pageSize",getPageSize());
		retMap.put("pageNo", getCurrentPage());
		retMap.put("pageCount", getPageCount());
		Optional.ofNullable(getRecordSet()).map(f->retMap.put("values",getRecordSet()));
		return retMap;
	}
	public void adjustPage(Integer total){
		if (total > 0) {
			int pages = total / getPageSize();
			if (total % getPageSize() != 0) {
				pages++;
			}
			setPageCount(pages);
		} else {
			setPageCount(0);
		}
	}
	public void addQueryParameter(Object[] values){
		if(values.length>0) {
			for(Object value:values) {
				queryParameters.add(value);
			}
		}
	}
	public void addNamedParameter(String key,Object value){
		namedParameters.put(key,value);
	}
	public void addQueryParameter(Collection<Object> values){
		queryParameters.addAll(values);
	}
	public static class Builder<T>{
		private PageQuery<T> pageQuery=new PageQuery<>();
		public Builder<T> setPageSize(Integer pageSize){
			pageQuery.setPageSize(pageSize);
			return this;
		}
		public Builder<T> setPageCount(Integer pageCount){
			pageQuery.setPageCount(pageCount);
			return this;
		}
		public Builder<T> setSelectedId(String selectedId){
			pageQuery.setSelectParamId(selectedId);
			return this;
		}
		public Builder<T> setCurrentPage(Integer pageNumber){
			pageQuery.setCurrentPage(pageNumber);
			return this;
		}
		public Builder<T> setOrder(String order){
			pageQuery.setOrder(order);
			return this;
		}
		public Builder<T> setOrderDir(String orderDir){
			pageQuery.setOrderDirection(orderDir);
			return this;
		}
		public Builder<T> addQueryParameterArr(Object... objects){
			pageQuery.getQueryParameters().addAll(Arrays.stream(objects).collect(Collectors.toList()));
			return this;
		}
		public Builder<T> addQueryParameter(Collection<Object> collection){
			pageQuery.addQueryParameter(collection);
			return this;
		}
		public Builder<T> addQueryParameter(Object[] obj){
			pageQuery.addQueryParameter(obj);
			return this;
		}

		public Builder<T> putNamedParameter(String key,Object value){
			pageQuery.getNamedParameters().put(key,value);
			return this;
		}
		public Builder<T> addParameter(String key,String value){
			pageQuery.getParameters().put(key,value);
			return this;
		}
		public Builder<T> setNamedParameters(Map<String,Object> namedParameter){
			pageQuery.setNamedParameters(namedParameter);
			return this;
		}
		public Builder<T> setParameters(Map<String,String> parameters){
			pageQuery.setParameters(parameters);
			return this;
		}
		public Builder<T> setConditions(String key,FilterCondition conditions){
			pageQuery.conditionMap.put(key,conditions);
			return this;
		}
		public PageQuery<T> build(){
			return pageQuery;
		}

	}

}
