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
package com.robin.comm.util.xls;

import lombok.Data;

@Data
public class TableHeaderColumn {
	private String columnName;
	private String columnCode;
	private int rowspan;
	private int colspan;
	private int startcol;
	private int startrow;
	public TableHeaderColumn(){
		
	}
	public TableHeaderColumn(String columnName,String columnCode,int rowspan,int colspan){
		this.columnName=columnName;
		this.rowspan=rowspan;
		this.colspan=colspan;
		this.columnCode=columnCode;
	}


}
