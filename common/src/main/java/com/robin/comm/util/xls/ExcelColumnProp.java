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

import com.robin.core.base.util.Const;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelColumnProp {
    private String columnName;
    private String columnCode;
    private Const.CLASSTYPE columnType;
    private String formula;
    private String format;
    private boolean needMerge;


    private ExcelColumnProp() {

    }
    public ExcelColumnProp(String columnName, String columnCode, Const.CLASSTYPE columnType){
        this.columnCode = columnCode;
        this.columnName = columnName;
        this.columnType = columnType;
    }
    public ExcelColumnProp(String columnName, String columnCode, Const.CLASSTYPE columnType, boolean needMerge) {
        this(columnName,columnCode,columnType);
        this.needMerge = needMerge;
    }
    public ExcelColumnProp(String columnName, String columnCode, Const.CLASSTYPE columnType, String formula) {
        this(columnName,columnCode,columnType);
        this.formula = formula;
    }



}
