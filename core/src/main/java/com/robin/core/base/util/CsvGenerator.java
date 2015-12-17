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
/**
 * <p>Title:  锟阶诧拷平台</p>
 *
 * <p>Description: 通锟斤拷Csv锟斤拷写锟斤拷锟斤拷锟斤拷</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: talkweb-BI</p>
 *
 * @author luoming
 * @version 1.0
 */
public class CsvGenerator{
	private static Logger logger=LoggerFactory.getLogger(CsvGenerator.class);
	/**
	 * CSV锟斤拷取
	 * @param inputStream   锟斤拷
	 * @param config        CSV锟斤拷锟斤拷
	 * @param columnResultList    锟斤拷锟?
	 * @return
	 */
	public static int ReadFile(InputStream inputStream,CsvConfig config,List<Map<String,String>> columnResultList){
		
		int pos=0;
		try{
			ICsvListReader reader=new CsvListReader(new InputStreamReader(inputStream,"UTF-8"),CsvPreference.STANDARD_PREFERENCE);
//			String[] header=reader.getCSVHeader(true);
//			if(header==null || header.length==0)
//				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
			//锟叫讹拷锟街讹拷锟角凤拷锟节憋拷锟叫达拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟睫筹拷,锟斤拷锟斤拷为全锟斤拷写锟斤拷全小写
			List<CsvColumnConfig> columnList=config.getConfigList();
//			for(int k=0;k<header.length;k++){
//				String column=columnList.get(k).getColumnName();
//				if(column==null)
//					column=columnMap.get(header[k].toLowerCase());
//				if(column!=null){
//					colList.add(Integer.valueOf(k));
//					ecolList.add(column);
//				}
//			}
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
//			String[] header=reader.getCSVHeader(true);
//			if(header==null || header.length==0)
//				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
			//锟叫讹拷锟街讹拷锟角凤拷锟节憋拷锟叫达拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟睫筹拷,锟斤拷锟斤拷为全锟斤拷写锟斤拷全小写
			List<CsvColumnConfig> columnList=config.getConfigList();
//			for(int k=0;k<header.length;k++){
//				String column=columnList.get(k).getColumnName();
//				if(column==null)
//					column=columnMap.get(header[k].toLowerCase());
//				if(column!=null){
//					colList.add(Integer.valueOf(k));
//					ecolList.add(column);
//				}
//			}
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
				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
			//锟叫讹拷锟街讹拷锟角凤拷锟节憋拷锟叫达拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟睫筹拷,锟斤拷锟斤拷为全锟斤拷写锟斤拷全小写
//			for(int k=0;k<header.length;k++){
//				String column=columnList.get(k).getColumnName();
//				if(column==null)
//					column=columnMap.get(header[k].toLowerCase());
//				if(column!=null){
//					colList.add(Integer.valueOf(k));
//					ecolList.add(column);
//				}
//			}
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
				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
			//List<Integer> colList=new ArrayList<Integer>();
			//List<Map<String, String>> ecolList=new ArrayList<Map<String,String>>();
			//锟叫讹拷锟街讹拷锟角凤拷锟节憋拷锟叫达拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟睫筹拷,锟斤拷锟斤拷为全锟斤拷写锟斤拷全小写
//			for(int k=0;k<header.length;k++){
//				String column=columnList.get(k).getColumnName();
//				if(column==null)
//					column=columnMap.get(header[k].toLowerCase());
//				if(column!=null){
//					colList.add(Integer.valueOf(k));
//					ecolList.add(column);
//				}
//			}
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
				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
			//锟叫讹拷锟街讹拷锟角凤拷锟节憋拷锟叫达拷锟节ｏ拷锟斤拷锟斤拷锟斤拷锟睫筹拷,锟斤拷锟斤拷为全锟斤拷写锟斤拷全小写
//			for(int k=0;k<header.length;k++){
//				String column=columnList.get(k).getColumnName();
//				if(column==null)
//					column=columnMap.get(header[k].toLowerCase());
//				if(column!=null){
//					colList.add(Integer.valueOf(k));
//					ecolList.add(column);
//				}
//			}
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
				throw new Exception("没锟斤拷锟斤拷锟斤拷头锟斤拷锟睫凤拷锟斤拷锟斤拷");
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
	/**
	 * 写锟斤拷CSV锟侥硷拷
	 * @param pwriter  PrintWriter
	 * @param header   头锟斤拷锟街凤拷锟叫憋拷
	 * @param resultList  锟斤拷菁锟斤拷锟?
	 * @param quotachar   锟街革拷锟斤拷
	 */
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
	/**
	 * 写锟斤拷CSV锟侥硷拷
	 * @param pwriter  PrintWirter
	 * @param header   头 锟斤拷锟斤拷为锟斤拷
	 * @param columnName  锟街讹拷锟叫憋拷
	 * @param list        锟斤拷菁锟?
	 * @param quotachar   锟街革拷锟斤拷
	 */
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
