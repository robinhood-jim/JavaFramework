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
	private String columnLength;
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

	public DataBaseColumnMeta() {
		
	}
	public DataBaseColumnMeta(String columnName,String columnType) {
		this.columnName=columnName;
		this.columnType=columnType;
	}
	public DataBaseColumnMeta(String columnName,String columnType,Integer length) {
		this.columnName=columnName;
		this.columnType=columnType;
		if(length!=null) {
            this.columnLength=String.valueOf(length);
        }
	}

}	
