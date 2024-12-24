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

import lombok.Data;

import java.io.Serializable;

@Data
public class DataBaseColumnMeta implements Serializable {
	private String columnName;
	private String columnType;
	private Integer dataType;
	private String typeName;
	private Integer columnLength;
	private int dataPrecise;
	private int dataScale;
	private String comment;
	private boolean primaryKey;
	private boolean foreignKey;
	private boolean nullable=true;
	private boolean increment;
	private String foreignTableName;
	private String foreignColumnName;
	private Integer keySeq;
	private String defaultValue;

	private DataBaseColumnMeta() {
		
	}
	public DataBaseColumnMeta(String columnName,String columnType) {
		this.columnName=columnName;
		this.columnType=columnType;
	}
	public DataBaseColumnMeta(String columnName,String columnType,Integer length) {
		this.columnName=columnName;
		this.columnType=columnType;
		if(length!=null) {
            this.columnLength=length;
        }
	}
	public static class Builder{
		private DataBaseColumnMeta meta=new DataBaseColumnMeta();
		public static Builder builder(){
			return new Builder();
		}
		public Builder columnName(String columnName){
			meta.setColumnName(columnName);
			return this;
		}
		public Builder columnType(String columnType){
			meta.setColumnType(columnType);
			return this;
		}
		public Builder dataType(Integer dataType){
			meta.setDataType(dataType);
			return this;
		}
		public Builder columnLength(Integer columnLength){
			meta.setColumnLength(columnLength);
			return this;
		}
		public Builder increment(boolean increment){
			meta.setIncrement(increment);
			return this;
		}
		public Builder defaultValue(String defaultValue){
			meta.setDefaultValue(defaultValue);
			return this;
		}
		public Builder primaryKey(boolean pkKey){
			meta.setPrimaryKey(pkKey);
			return this;
		}
		public Builder nullable(boolean nullable){
			meta.setNullable(nullable);
			return this;
		}
		public Builder precise(int precise){
			meta.setDataPrecise(precise);
			return this;
		}
		public Builder scale(int scale){
			meta.setDataScale(scale);
			return this;
		}
		public Builder comment(String comment){
			meta.setComment(comment);
			return this;
		}
		public Builder typeName(String typeName){
			meta.setTypeName(typeName);
			return this;
		}
		public DataBaseColumnMeta build(){
			return meta;
		}


	}

}	
