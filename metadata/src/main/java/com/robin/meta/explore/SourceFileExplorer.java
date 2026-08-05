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
package com.robin.meta.explore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionsUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Description:Source DataFile Explorer</p>
 */
@Slf4j
public class SourceFileExplorer {
    private static final SimpleDateFormat full_format =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    private static final SimpleDateFormat short_format =new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat day_format =new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat digital_format =new SimpleDateFormat("yyyyMMddhhmmss");
    private static final SimpleDateFormat normal_format =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private SourceFileExplorer(){

    }
    public static void exploreCsv(BufferedReader reader,DataCollectionMeta meta,String[] headers, int readLines){
        int columnPos=1;
        String[] readHeader=headers;
        String spiltChar=getSpiltChar(meta.getSplit());
        try (ICsvListReader ireader=new CsvListReader(reader,new CsvPreference.Builder( '"', spiltChar.charAt(0), "n").build())){

            if(headers==null){
                readHeader=ireader.getHeader(true);
            }
            List<String> resultlist;
            Map<String,Map<Const.CLASSTYPE, Long>> typeMap=new HashMap<>();
            Map<String,Map<String, Long>> dateFormatMap=new HashMap<>();
            while((resultlist=ireader.read())!=null && columnPos<readLines){
                for(int pos=0;pos<headers.length;pos++){
                    getTypeByData(resultlist.get(pos),readHeader[pos],typeMap,dateFormatMap);
                }
                columnPos++;
            }
            adjustColumnTypeAndFormat(readHeader,typeMap,dateFormatMap,meta);

        }catch (Exception ex){
            log.error("",ex);
        }
    }
    public static void exploreJson(BufferedReader reader,DataCollectionMeta meta,int readLines){
        int columnPos=1;
        Gson gson= GsonUtil.getGson();
        Map<String,Map<Const.CLASSTYPE, Long>> typeMap=new HashMap<>();
        Map<String,Map<String, Long>> dateFormatMap=new HashMap<>();
        List<String> columns=new ArrayList<>();
        try{
            String lineStr;
            while((lineStr=reader.readLine())!=null && columnPos<readLines) {
                Map<String,Object> map=gson.fromJson(lineStr,new TypeToken<Map<String,Object>>(){}.getType());
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String column = entry.getKey();
                    if (columnPos == 1 && !columns.contains(column)) {
                        columns.add(column);
                    }
                    getTypeByData(entry.getValue().toString(), column, typeMap, dateFormatMap);
                }
                columnPos++;
            }
            adjustColumnTypeAndFormat(columns.toArray(new String[]{""}),typeMap,dateFormatMap,meta);

        }catch (Exception ex){
            ex.printStackTrace();
            log.error("",ex);
        }
    }
    private static void adjustColumnTypeAndFormat(String[] columnNames,Map<String,Map<Const.CLASSTYPE,Long>> typeMap,Map<String,Map<String,Long>> dateFormatMap,DataCollectionMeta meta){
        for(String columnName:columnNames) {
            List<Map.Entry<Const.CLASSTYPE, Long>> sortList = CollectionsUtil.getSortedMapByLongValue(Const.CLASSTYPE.class,typeMap.get(columnName), false);
            if(dateFormatMap.containsKey(columnName)){
                List<Map.Entry<String,Long>> dateFormatList=CollectionsUtil.getSortedMapByLongValue(String.class, dateFormatMap.get(columnName),false);
                meta.addColumnMeta(columnName,sortList.get(0).getKey(),null,false,dateFormatList.get(0).getKey());
            }else{
                //data with Same type
                if(sortList.size()==1){
                    meta.addColumnMeta(columnName,sortList.get(0).getKey(),null);
                }else{
                    if(sortList.get(0).getKey().equals(Const.CLASSTYPE.INTEGER)){
                        //second large type contains less than 5 percent of primary type, skip secondary type
                        if(sortList.get(1).getValue().doubleValue()/sortList.get(0).getValue().doubleValue()<=0.05){
                            meta.addColumnMeta(columnName,sortList.get(0).getKey(),null);
                        }else{
                            meta.addColumnMeta(columnName, Const.CLASSTYPE.NUMERIC,null);
                        }
                    }else{
                        meta.addColumnMeta(columnName,sortList.get(0).getKey(),null);
                    }
                }

            }
        }
    }
    private static void getTypeByData(String targetValue,String columnName, Map<String,Map<Const.CLASSTYPE, Long>> dataTypeMap, Map<String,Map<String, Long>> dateFomatMap){
        Const.CLASSTYPE type;
        boolean isDate=false;
        if(NumberUtils.isDigits(targetValue) && !targetValue.contains(".")){
            try{
                Integer.parseInt(targetValue);
                type= Const.CLASSTYPE.INTEGER;
            }catch(Exception ex){
                type= Const.CLASSTYPE.LONG;
            }
            addDataWithType(type,columnName, dataTypeMap);
        }else if(NumberUtils.isNumber(targetValue)){
            addDataWithType(Const.CLASSTYPE.DOUBLE,columnName, dataTypeMap);
        }
        else {
            if(!isDateWithType(columnName,normal_format, targetValue, "yyyy-MM-dd hh:mm:ss",dataTypeMap,dateFomatMap)){
                if(!isDateWithType(columnName,short_format, targetValue, "yyyyMMdd",dataTypeMap, dateFomatMap)){
                    if(!isDateWithType(columnName,day_format, targetValue, "yyyy-MM-dd",dataTypeMap,dateFomatMap)){
                        if(!isDateWithType(columnName,digital_format, targetValue, "yyyyMMddhhmmss",dataTypeMap, dateFomatMap)){
                            if(isDateWithType(columnName,full_format, targetValue, "yyyy-MM-dd hh:mm:ss.SSS",dataTypeMap, dateFomatMap)){
                                isDate=true;
                            }
                        }else{
                            isDate=true;
                        }
                    }else{
                        isDate=true;
                    }
                }else{
                    isDate=true;
                }
            }else{
                isDate=true;
            }
            if(!isDate){
                addDataWithType(Const.CLASSTYPE.STRING,columnName, dataTypeMap);
            }else{
                addDataWithType(Const.CLASSTYPE.TIMESTAMP,columnName, dataTypeMap);
            }
        }

    }
    private static boolean isDateWithType(String columnName, SimpleDateFormat format, String datastr, String formatstr,Map<String,Map<Const.CLASSTYPE, Long>> typeMap, Map<String,Map<String, Long>> dateFormatMap){
        boolean isok=false;
        try{
            if(format.parse(datastr)!=null && datastr.length()==formatstr.length()){
                addDataWithType(Const.CLASSTYPE.DATE,columnName, typeMap);
                Map<String,Long> map=new HashMap<>();
                map.put(formatstr,0L);
                dateFormatMap.putIfAbsent(columnName,map);
                dateFormatMap.get(columnName).put(formatstr,dateFormatMap.get(columnName).get(formatstr)+1);
                isok=true;
            }
        }catch(Exception ex){

        }
        return isok;
    }
    private static boolean isTimestampWithType(String columnName, SimpleDateFormat format, String datastr, String formatstr, Map<String,Map<Const.CLASSTYPE, Long>> dateFormatMap){
        boolean isok=false;
        try{
            if(format.parse(datastr)!=null && datastr.length()==formatstr.length()){
                addDataWithType(Const.CLASSTYPE.TIMESTAMP,columnName, dateFormatMap);
                isok=true;
            }
        }catch(Exception ex){

        }
        return isok;
    }


    private static void addDataWithType(Const.CLASSTYPE dbtype, String columnName, Map<String,Map<Const.CLASSTYPE, Long>> typeMap){
        Map<Const.CLASSTYPE,Long> tmap=new HashMap<>();
        tmap.put(dbtype,0L);
        typeMap.putIfAbsent(columnName,tmap);
        typeMap.get(columnName).put(dbtype, typeMap.get(columnName).get(dbtype) + 1);
    }
    public static final String getSpiltChar(String split){
        String tmpSplit=split;
        if(Const.ESCAPE_CHARACTERS.contains(split)) {
            tmpSplit = "\\"+split;
        }
        return tmpSplit;
    }

}
