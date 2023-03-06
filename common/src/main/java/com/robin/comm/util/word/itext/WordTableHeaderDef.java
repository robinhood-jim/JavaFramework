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
package com.robin.comm.util.word.itext;

import java.util.ArrayList;
import java.util.List;

public class WordTableHeaderDef {
	private List<List<WordTableColumnDef>> columnList=new ArrayList<List<WordTableColumnDef>>();
	private int headerNums=1;
	
	public List<List<WordTableColumnDef>> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<List<WordTableColumnDef>> columnList) {
		this.columnList = columnList;
	}

	public int getHeaderNums() {
		return headerNums;
	}

	public void setHeaderNums(int headerNums) {
		this.headerNums = headerNums;
	}

}
