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

import java.util.ArrayList;
import java.util.List;

@Data
public class TableConfigProp {

    private int containrow;
    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean useTreeCfg = false;
    private String headerFontName;
    private String contentFontName;
    private int totalCol;
    private int colSize;
    private List<String> columnCodeList = new ArrayList<String>();


    private List<TableMergeRegion> headerList = new ArrayList<TableMergeRegion>();
    private List<List<TableHeaderColumn>> headerColumnList = new ArrayList<List<TableHeaderColumn>>();

    public void addMerginRegion(String name, int startcol, int startrow, int length, int collength, int colheight) {
        TableMergeRegion region = new TableMergeRegion(name, startcol, startrow, length, collength, colheight);
        headerList.add(region);
    }

    public void addSubMergion(TableMergeRegion parent, TableMergeRegion child) {
        parent.addSubRegion(child);
    }

    public List<List<TableHeaderColumn>> getHeaderColumnList() {
        return headerColumnList;
    }

    public void setHeaderColumnList(List<List<TableHeaderColumn>> headerColumnList) {
        this.containrow=headerColumnList.size();
        this.headerColumnList = headerColumnList;
    }
    public static class Builder{
        public static  TableConfigProp prop=new TableConfigProp();
        public Builder(){

        }
        public TableConfigProp.Builder setFont(String fontName){
            prop.setHeaderFontName(fontName);
            prop.setContentFontName(fontName);
            return this;
        }
        public TableConfigProp.Builder setHeaderFont(String fontName){
            prop.setHeaderFontName(fontName);
            return this;
        }
        public TableConfigProp.Builder setContentFont(String fontName){
            prop.setContentFontName(fontName);
            return this;
        }

        public TableConfigProp.Builder setBold(boolean ifbold){
            prop.setBold(ifbold);
            return this;
        }
        public TableConfigProp.Builder setHeaderColumnList(List<List<TableHeaderColumn>> headerColumnList){
            prop.containrow=headerColumnList.size();
            prop.headerColumnList = headerColumnList;
            return this;
        }

        public TableConfigProp build(){
            return prop;
        }

    }


}
