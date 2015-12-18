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
package com.robin.core.base.util;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class CsvGenerator{
	private static Logger logger=LoggerFactory.getLogger(CsvGenerator.class);

	public static int ReadFile(InputStream inputStream,CsvConfig config,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(new InputStreamReader(inputStream,"UTF-8"),CsvPreference.STANDARD_PREFERENCE);
			List<CsvColumnConfig> columnList=config.getConfigList();
			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<columnList.size();j++){
					CsvColumnConfig colconfig=columnList.get(j);
					resultMap.put(colconfig.getColumnCode(), resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(InputStream inputStream,CsvConfig config,char separator,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(new InputStreamReader(inputStream,"UTF-8"),new CsvPreference('"', separator, "\r\n"));
			List<CsvColumnConfig> columnList=config.getConfigList();
			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<columnList.size();j++){
					CsvColumnConfig colconfig=columnList.get(j);
					resultMap.put(colconfig.getColumnCode(), resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(InputStream inputStream,List<Map<String,String>> columnResultList){
	
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(new InputStreamReader(inputStream,"UTF-8"),CsvPreference.STANDARD_PREFERENCE);
			String[] header=reader.getCSVHeader(true);
			if(header==null || header.length==0)
				throw new Exception("no file");
			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<header.length;j++){
					resultMap.put(header[j], resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(InputStream inputStream,char seperator,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(new InputStreamReader(inputStream,"UTF-8"),new CsvPreference('"', seperator, "\r\n"));
			String[] header=reader.getCSVHeader(true);
			if(header==null || header.length==0)
				throw new Exception("no file");

			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<header.length;j++){
					resultMap.put(header[j], resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(Reader ireader,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(ireader,CsvPreference.STANDARD_PREFERENCE);
			String[] header=reader.getCSVHeader(true);
			if(header==null || header.length==0)
				throw new Exception("no file");
			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<header.length;j++){
					resultMap.put(header[j], resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(Reader ireader,CsvConfig config,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(ireader,CsvPreference.STANDARD_PREFERENCE);
			List<CsvColumnConfig> columnList=config.getConfigList();

			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<columnList.size();j++){
					CsvColumnConfig colconfig=columnList.get(j);
					resultMap.put(colconfig.getColumnCode(), resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(Reader ireader,char seperator,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(ireader,new CsvPreference('"', seperator, "\n"));
			String[] header=reader.getCSVHeader(true);
			if(header==null || header.length==0)
				throw new Exception("no file ");
			List<String> resultlist=new ArrayList<String>();
			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<header.length;j++){
					resultMap.put(header[j], resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static int ReadFile(Reader ireader,char seperator,CsvConfig config,List<Map<String,String>> columnResultList){
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(ireader,new CsvPreference('"', seperator, "\n"));
			List<String> resultlist=new ArrayList<String>();
			List<CsvColumnConfig> columnList=config.getConfigList();

			while((resultlist=reader.read())!=null){
				pos++;
				Map<String, String> resultMap=new HashMap<String, String>();
				for(int j=0;j<columnList.size();j++){
					CsvColumnConfig colconfig=columnList.get(j);
					resultMap.put(colconfig.getColumnCode(), resultlist.get(j));
				}
				columnResultList.add(resultMap);	
			}
		}catch (Exception e) {
			e.printStackTrace();	
		}
		return pos;
	}
	public static void WriteFile(PrintWriter pwriter,String[] header, List<String[]> resultList,String quotachar){
		try{
			ICsvListWriter writer=new CsvListWriter(pwriter,CsvPreference.STANDARD_PREFERENCE);
			writer.writeHeader(header);
			if(quotachar!=null)
			{
				CsvPreference preference=new CsvPreference(quotachar.charAt(0),0,"\n");
				writer.setPreferences(preference);
			}
			for(String[] strArr:resultList)
				writer.write(strArr);
			writer.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void WriteDataToFile(PrintWriter pwriter,String[] header,String[] columnName,List<Map<String, String>> list,String quotachar){
		try{
			ICsvListWriter writer=new CsvListWriter(pwriter,CsvPreference.STANDARD_PREFERENCE);
			if(header!=null)
				writer.writeHeader(header);
			if(quotachar!=null)
			{
				CsvPreference preference=new CsvPreference('"',quotachar.charAt(0),"\n");
				writer.setPreferences(preference);
			}
			for(Map<String, String> map:list){
				String[] strArr=new String[header.length];
				for(int i=0;i<columnName.length;i++){
					strArr[i]=map.get(columnName[i]);
				}
				writer.write(strArr);  
			}
			writer.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void WriteDataToFile(String fileName,String[] header,String[] columnName,List<Map<String, String>> list,String quotachar){
		PrintWriter pwriter=null;
		ICsvListWriter writer=null;
		try{
			pwriter=new PrintWriter(new File(fileName));
			writer=new CsvListWriter(pwriter,CsvPreference.STANDARD_PREFERENCE);
			if(header!=null)
				writer.writeHeader(header);
			if(quotachar!=null)
			{
				CsvPreference preference=new CsvPreference('"',quotachar.charAt(0),"\n");
				writer.setPreferences(preference);
			}
			for(Map<String, String> map:list){
				String[] strArr=new String[columnName.length];
				for(int i=0;i<columnName.length;i++){
					if(map.get(columnName[i])!=null)
						strArr[i]=map.get(columnName[i]);
					else
						strArr[i]="";
				}
				writer.write(strArr);  
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if (writer!=null) {
					writer.close();
				}
				if(pwriter!=null)
					pwriter.close();
			}catch(Exception ex){
				ex.printStackTrace();
				logger.error("Encounter error",ex);
			}
		}
	}

}