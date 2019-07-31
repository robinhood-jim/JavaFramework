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
package com.robin.core.web.codeset;

import lombok.Data;

@Data
public class Code {
    private String codeName;
    private String value;
    private String parentCodeId;
    private String parentCodeValue;
    public Code(String codeName, String value)
    {
        this.codeName = codeName;
        this.value = value;
        this.parentCodeId = null;
        this.parentCodeValue = null;
    }

    public Code(String codeName, String value, String pcodeid, String pvalue)
    {
        this.codeName = codeName;
        this.value = value;
        this.parentCodeId = pcodeid;
        this.parentCodeValue = pvalue;
    }
    public Code(){

    }

}
