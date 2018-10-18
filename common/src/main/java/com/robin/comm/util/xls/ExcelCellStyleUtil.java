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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import com.robin.core.base.util.Const;

public class ExcelCellStyleUtil {
	private static volatile ExcelCellStyleUtil util;
	private Map<String,CellStyle> cellMap=new HashMap<String, CellStyle>();
	private static String defaultFontName="Microsoft YaHei";
	private ExcelCellStyleUtil(){
		
	}
	public static ExcelCellStyleUtil getInstance(){
		if(util==null){
			synchronized (ExcelCellStyleUtil.class) {
				if(util==null)
					util=new ExcelCellStyleUtil();
			}
		}
		return util;
	}
	public CellStyle getCellStyle(Workbook wb,int rowspan,int colspan,String metaType,TableHeaderProp header){
		CellStyle cs=null;
		if(cellMap.containsKey("C_"+rowspan+"_"+colspan+"_"+metaType)){
			cs= cellMap.get("C_"+rowspan+"_"+colspan+"_"+metaType);
		}else{
			cs=wb.createCellStyle();
			if (rowspan > 1)  
	            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);  
	        cs.setAlignment(CellStyle.ALIGN_CENTER);  
	        cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	        cs.setBorderLeft(CellStyle.BORDER_THIN); 
	        cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	        cs.setBorderRight(CellStyle.BORDER_THIN);
	        cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
	        cs.setBorderTop(CellStyle.BORDER_THIN);  
	        cs.setTopBorderColor(IndexedColors.BLACK.getIndex());
	        cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
	        	 if(header!=null){
	             	Font font=wb.createFont();
	             	if(header.getFontName()!=null && header.getFontName().equals(""))
	             		font.setFontName(header.getFontName());
	             	else
	             		font.setFontName(defaultFontName);
	             	if(header.isBold())
	             	{
	             		font.setBoldweight((short)2);
	             	}
	             	if(header.isItalic()){
	             		font.setItalic(true);
	             	}
	             	cs.setFont(font);
	             }
	        cs.setFillPattern(CellStyle.SOLID_FOREGROUND);  
	        cs.setWrapText(true);
	        if(metaType.equals(Const.META_TYPE_NUMERIC)){
				cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.0"));
			}else if(metaType.equals(Const.META_TYPE_DATE)){
				  cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd hh:mm:ss"));
			}else if(metaType.equals(Const.META_TYPE_INTEGER)){
				cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
			}
	        cellMap.put("C_"+rowspan+"_"+colspan+"_"+metaType, cs);
		}
		return cs;  
	}
	

}
