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

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.DataTypeEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ExcelGenerator {
	private static Log log=LogFactory.getLog(ExcelGenerator.class);
	public static void ReadExcelFile(String filename,ExcelSheetProp prop) throws Exception{
		InputStream myxls = new FileInputStream(filename);
		
		try{
			ReadExcelFile(myxls, prop);
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new Exception("Read Error!");
		}
	}
	
	
	
	
	public static void ReadExcelFile(InputStream myxls,ExcelSheetProp prop) throws Exception{
		boolean is2007=ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileext());
		Workbook wb=null;
		if(is2007){
			wb	=	new XSSFWorkbook(myxls);
		}else  {
			wb     = new HSSFWorkbook(myxls);
		}
		
		Sheet sheet = wb.getSheetAt(0); 
		List<Map<String,String>> columnValueList=new ArrayList<Map<String,String>>();
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//hh24:mi:ss
		int pos=0;
		for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			pos++;
			if (pos < prop.getStartRow()) { 
				rit.next();
				continue;
			}
			Map<String, String> listMap = new HashMap<String, String>();
			Row row =  rit.next();

			int j = 0;
			int endpos=prop.getStartCol()+prop.getColumnPropList().size()-1;
			boolean ishasrecord=false;
			for (int i = prop.getStartCol()-1; i < endpos; i++) {
				if(i>=prop.getColumnPropList().size())
					break;
				String type=prop.getColumnPropList().get(i).getColumnType();
				
				Cell cell =  row.getCell(i);
				String strCell = "";
                
                if(cell!=null){
                	switch (cell.getCellType()) {
                	case Cell.CELL_TYPE_NUMERIC:
                		if (HSSFDateUtil.isCellDateFormatted(cell) || type.equals(Const.META_TYPE_DATE)) {
                			Date date = cell.getDateCellValue();
                			strCell = format.format(date);
                		} else if(type.equals(Const.META_TYPE_INTEGER) || type.equals(Const.META_TYPE_DOUBLE)){
                			if(type.equals(Const.META_TYPE_INTEGER))
                				strCell = String.valueOf((int)cell.getNumericCellValue());
                			else
                				strCell = String.valueOf(cell.getNumericCellValue());
                		}
                		else if(type.equals(Const.META_TYPE_STRING)){
                			double d=cell.getNumericCellValue();
                			DecimalFormat   df  = new   DecimalFormat("#.#");
                			strCell=df.format(d);
//                			String str1=String.valueOf(Double.valueOf(df.format(d)).intValue());
//							if(str1!=null && !"".equals(str1.trim()))
//                			strCell=String.valueOf(Double.valueOf(d).intValue());
                		}
                		break;
					case Cell.CELL_TYPE_STRING:
						strCell = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						strCell = String.valueOf(cell.getBooleanCellValue());
						break;
					default:
						strCell = "";
						break;
                	}
                	//listMap.put(prop.getColumnName()[j], strCell);
                }
                if(strCell!=null && !"".equals(strCell.trim()))
                	ishasrecord=true;
                listMap.put(prop.getColumnPropList().get(j).getColumnCode(), strCell);
				j++;
			}
			if(ishasrecord)
				columnValueList.add(listMap);
		}
		prop.setColumnList(columnValueList);
	}
	public static int ReadExcelFile(InputStream myxls,int sheetIndex,Map<String,DataTypeEnum> columnMap,String startPos,String endPos,ExcelSheetProp prop) {
		int pos=0;
		List<Map<String,String>> columnValueList=new ArrayList<Map<String,String>>();
		try{
			boolean is2007=ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileext());
			Workbook wb=null;
			if(is2007){
				wb	=	new XSSFWorkbook(myxls);
			}else  {
				wb     = new HSSFWorkbook(myxls);
			}
			
			Sheet sheet = wb.getSheetAt(sheetIndex);
		
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//hh24:mi:ss
		List<DataTypeEnum> columnList=new ArrayList<DataTypeEnum>();
		List<Integer> collist=new ArrayList<Integer>();
		int[] excelPos=getExcelPosition(startPos, endPos);
		System.out.println(excelPos[2]+"  "+excelPos[3]);
		int startCol=excelPos[0];
		int endCol=excelPos[1];
		int startRow=excelPos[2];
		int endRow=excelPos[3];
		for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			pos++;
			Row row =  rit.next();
			if(pos==startRow){
				for(int i=startCol-1;i<endCol;i++){
					Cell cell = (Cell) row.getCell(i);
					String cellName=cell.getStringCellValue();
					DataTypeEnum column=columnMap.get(cellName.toUpperCase());
					if(column==null)
						column=columnMap.get(cellName.toLowerCase());
					if(column!=null)
					{
						collist.add(Integer.valueOf(i));
						columnList.add(column);
					}
				}
				continue;
			}
			else if (pos < startRow) { 
				continue;
			}
			else if(pos > endRow)
				break;
			Map<String, String> listMap = new HashMap<String, String>();
			
			boolean ishasrecord=false;
			
			
			for (int j=0;j<collist.size();j++) {
				int colpos=collist.get(j).intValue();
				DataTypeEnum column=columnList.get(j);
				String type=column.getDataType();
				HSSFCell cell = (HSSFCell) row.getCell(colpos);
				String strCell = "";   
                if(cell!=null){
                	switch (cell.getCellType()) {
                	case HSSFCell.CELL_TYPE_NUMERIC:
                		if (HSSFDateUtil.isCellDateFormatted(cell) || type.equals(String.valueOf(Const.FIELD_TYPE_DATE))) {
                			double d = cell.getNumericCellValue();
                			Date date = HSSFDateUtil.getJavaDate(d);
                			strCell = format.format(date);
                		} else if(type.equals(Const.META_TYPE_NUMERIC))
                			strCell = String.valueOf(cell.getNumericCellValue());
                		else if(type.equals(Const.META_TYPE_STRING)){
                			double d=cell.getNumericCellValue();
                			String str1=String.valueOf(Double.valueOf(d).intValue());
							if(str1!=null && !"".equals(str1.trim()))
                			strCell=String.valueOf(Double.valueOf(d).intValue());
                		}
                		break;
					case HSSFCell.CELL_TYPE_STRING:
						if(type.equals(Const.META_TYPE_NUMERIC)){
							strCell = cell.getStringCellValue();
							double d=Double.valueOf(strCell);
						}else if(type.equals(Const.META_TYPE_DATE)){
							double d = cell.getNumericCellValue();
                			Date date = HSSFDateUtil.getJavaDate(d);
						}else if(type.equals(Const.META_TYPE_STRING))
							strCell = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						strCell = String.valueOf(cell.getBooleanCellValue());
						break;
					default:
						strCell = "";
						break;
                	}
                	//listMap.put(prop.getColumnName()[j], strCell);
                }
                if(strCell!=null && !"".equals(strCell.trim()))
                	ishasrecord=true;
                listMap.put(column.getName(), strCell);
			}
			
			if(ishasrecord)
				columnValueList.add(listMap);
			}
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("pos="+pos+"column size="+columnValueList.size());
				log.error(e);
			}
		
		prop.setColumnList(columnValueList);
		return pos;
	}
	public static void readExcelHeader(InputStream myxls,ExcelSheetProp prop){
		
		
	}
	public int getAvaiableRows(InputStream myxls,int startRow) throws Exception{
		HSSFWorkbook wb     = new HSSFWorkbook(myxls);
		HSSFSheet sheet = wb.getSheetAt(0);
		int num=sheet.getLastRowNum();
		return num-startRow;
	}
	/**
	 * Xls For 2003,xlsx for 2007
	 * @param prop
	 * @param suffix
	 * @return
	 */

	public static Workbook GenerateExcelFile(ExcelSheetProp prop,String suffix){
		boolean is2007=ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileext());
		Workbook wb =null;
		if(!is2007)
			wb=new HSSFWorkbook();
		else{
			wb=new XSSFWorkbook();
		}
		String sheetname=prop.getSheetName();
		if(suffix!=null && suffix.length()>0)
			sheetname+="_"+suffix;
		Sheet sheet = wb.createSheet(sheetname);
		GenerateHeader(sheet,wb,prop);
		
		FillColumns(wb,sheet,prop);
		autoSizeSheet(sheet, prop.getColumnName().length);
		return wb;
	}
	/**
	 * 
	 * @param prop
	 * @param header
	 * @return
	 */
	public static Workbook GenerateExcelFile(ExcelSheetProp prop,TableHeaderProp header) throws Exception{
		Workbook wb = ExcelBaseOper.creatWorkBook(prop);
		String sheetname=prop.getSheetName();
		Sheet sheet = wb.createSheet(sheetname);
		CreationHelper helper=wb.getCreationHelper();
		int count=0;
		if(!prop.getColumnPropList().isEmpty()){
			GenerateHeader(sheet, wb, prop,header);
			count=prop.getColumnList().size();
		}
		else{
			int rows=header.getContainrow();
		
			count=GenerateHeader(sheet, wb, prop, header,helper);
		}
		FillColumns(wb, sheet, prop, header, helper);
		autoSizeSheet(sheet, count);
		return wb;
	}
	public static Workbook GenerateExcelFile(ExcelSheetProp prop, TableHeaderProp header, Connection conn, String querySql, Object[] queryParam, ExcelRsExtractor extractor) throws Exception{
		Workbook wb = ExcelBaseOper.creatWorkBook(prop);
		String sheetname=prop.getSheetName();
		Sheet sheet = wb.createSheet(sheetname);
		CreationHelper helper=wb.getCreationHelper();
		extractor.setWorkbook(wb);
		extractor.setTargetSheet(sheet);
		extractor.setHelper(helper);
		int count=0;
		if(!header.getHeaderList().isEmpty()){
			GenerateHeader(sheet, wb, prop,header);
			count=prop.getColumnList().size();
		}
		else{
			GenerateHeader(sheet, wb, prop, header);
		}
		try{
			SimpleJdbcDao.executeOperationWithQuery(conn,querySql,queryParam,extractor);
		}catch (Exception ex){
			ex.printStackTrace();
		}
		//FillColumns(wb, sheet, prop, header, helper);
		autoSizeSheet(sheet, count+1);
		return wb;
	}
	private static void GenerateExcelFile(Workbook wb,ExcelSheetProp prop,TableHeaderProp header) throws Exception{
		String sheetname=prop.getSheetName();
		Sheet sheet = wb.createSheet(sheetname);
		CreationHelper helper=wb.getCreationHelper();
		int count=0;
		if(header==null){
			GenerateHeader(sheet, wb, prop,header);
			count=prop.getColumnList().size();
		}
		else{
		    int containrow=header.getContainrow();
			count=GenerateHeader(sheet, wb, prop, header,helper);
			header.setContainrow(containrow);
		}
		FillColumns(wb, sheet, prop, header, helper);
		autoSizeSheet(sheet, count);
	}
	/**
	 * Generate with Mutil Sheet
	 * @param propList
	 * @return
	 */
	public static Workbook GenerateExcelFileWithMutilSheet(List<ExcelProperty> propList) throws Exception{
		Workbook wb = ExcelBaseOper.creatWorkBook(propList.get(0).getSheetProp());
		
		for (ExcelProperty prop:propList) {
			GenerateExcelFile(wb,prop.getSheetProp(), prop.getTableProp());
		}
		return wb;
	}
	
	private static void GenerateHeader(Sheet targetsheet,Workbook wb,ExcelSheetProp prop)
	{
		Row row = targetsheet.createRow(0);
		for (int i = 0; i < prop.getHeaderName().length; i++) {
			String values = prop.getHeaderName()[i];
			if(values!=null&& !"".equals(values))
			{
				Cell cel=row.createCell(i);
				
				CellStyle cellStyle = ExcelBaseOper.getStyle(wb, 1, CellStyle.ALIGN_CENTER,null);
				cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				cel.setCellValue(values);
				cel.setCellStyle(cellStyle);
			}
		}
	}


	private static void GenerateHeader(Sheet targetsheet,Workbook wb,ExcelSheetProp prop,TableHeaderProp header)
	{
		Row row = targetsheet.createRow(prop.getStartRow()-1);
		for (int i = 0; i < prop.getColumnPropList().size(); i++) {
			String values = prop.getColumnPropList().get(i).getColumnName();
			if(values!=null&& !"".equals(values))
			{
				Cell cel=row.createCell(i);
				
				CellStyle cellStyle = ExcelBaseOper.getStyle(wb, 1, CellStyle.ALIGN_CENTER,header);
				//cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				cel.setCellStyle(cellStyle);
				cel.setCellValue(values);
				
			}
		}
	}

	private static int GenerateHeader(Sheet targetsheet,Workbook wb,ExcelSheetProp prop,TableHeaderProp header,CreationHelper helper)
	{
		int startcol=prop.getStartCol()-1;
		int startrow=prop.getStartRow()-1;
		
		int contianrows=header.getContainrow();
		Row[] headerRow=new Row[contianrows];
		 int tmpcount=startcol;
		 for (int pos = 0; pos < contianrows; pos++) {
				headerRow[pos] = ExcelBaseOper.creatRow(targetsheet, startrow
						+ pos);
			}
		if(header.getHeaderList().size()!=0){
			for (int i = 0; i < header.getHeaderList().size(); i++) {
				TableMergeRegion region = header.getHeaderList().get(i);
				String name = region.getName();
				createCellRegion(targetsheet, wb, prop, header, region,
						headerRow, helper, 0, tmpcount, name);
				tmpcount += region.getCollength();
			}
		}else if(header.getHeaderColumnList().size()!=0){
			int[][] colArr=MergeCellUtil.caculateHeaderRowStartcol(header);
			tmpcount=header.getHeaderColumnList().size();
			for (int i = 0; i < header.getHeaderColumnList().size(); i++) {
				List<TableHeaderColumn> list=header.getHeaderColumnList().get(i);
				for (int j = 0; j < list.size(); j++) {
					TableHeaderColumn column=list.get(j);
					column.setStartcol(colArr[i][j]);
					createCellRegion(targetsheet, wb, header, header.getHeaderColumnList().get(i).get(j), headerRow, helper);
				}
			}
		}else if(!prop.getColumnPropList().isEmpty()){
			GenerateHeader(targetsheet,wb,prop,header);
			tmpcount=prop.getStartRow();
		}
		for (int i = 0; i < header.getHeaderList().size(); i++) {
			TableMergeRegion region=header.getHeaderList().get(i);
			createCellRegion(targetsheet, wb, header, region, headerRow, helper);
		}
		return tmpcount;
		
	}
	

	private static void createCellRegion(Sheet targetsheet,Workbook wb,ExcelSheetProp prop,TableHeaderProp header,TableMergeRegion region,Row[] headerRows,CreationHelper helper,int level,int startcol,String value){
		
		String name=region.getName();
		Row baserow=headerRows[level];
		int baseRow=baserow.getRowNum();
		if(name!=null&& !"".equals(name))
		{
			if(region.getSubRegions().size()==0){
				if(region.getColheigth()==1 && region.getCollength()==1){
					CellStyle style=ExcelBaseOper.getStyle(wb, 1, CellStyle.ALIGN_CENTER,region,header);
					Cell cell=ExcelBaseOper.createCell(baserow, startcol, value, Const.META_TYPE_STRING, style,helper);
					cell.setCellValue(name);
				}else{
					CellStyle style=ExcelBaseOper.getStyle(wb, 3, CellStyle.ALIGN_CENTER,region,header);
					ExcelBaseOper.merged(targetsheet, baserow,Const.META_TYPE_STRING, baseRow, startcol, baseRow+region.getColheigth()-1, startcol+region.getCollength()-1, style, value,helper);
				}		
			}else{
				CellStyle style=ExcelBaseOper.getStyle(wb, 3, HSSFCellStyle.ALIGN_CENTER,region,header);
				ExcelBaseOper.merged(targetsheet, baserow,Const.META_TYPE_STRING,baseRow, startcol, baseRow+region.getColheigth()-1, startcol+region.getCollength()-1, style, value,helper);
			}
		}
		int tmpcol=startcol;
		if(region.getSubRegions().size()!=0){
			for (int j = 0; j < region.getSubRegions().size(); j++) {
				TableMergeRegion newregion=region.getSubRegions().get(j);
				createCellRegion(targetsheet, wb, prop, header, newregion, headerRows,helper, level+1,tmpcol, newregion.getName());
				tmpcol+=newregion.getCollength();
			}
		}
	}
	private static void createCellRegion(Sheet targetsheet,Workbook wb,TableHeaderProp prop,TableHeaderColumn column,Row[] headerRows,CreationHelper helper){
		CellStyle style=null;
		Row baseRow=headerRows[column.getStartrow()];
		if(column.getRowspan()>1 || column.getColspan()>1){
			style=ExcelBaseOper.getStyle(wb, 3, CellStyle.ALIGN_CENTER,prop);
			ExcelBaseOper.merged(targetsheet, baseRow,Const.META_TYPE_STRING, column.getStartrow(), column.getStartcol(), column.getStartrow()+column.getRowspan()-1, column.getStartcol()+column.getColspan()-1, style, column.getColumnName(),helper);
		}
		else{
			style=ExcelBaseOper.getStyle(wb, 1, CellStyle.ALIGN_CENTER,prop);
			ExcelBaseOper.createCell(baseRow, column.getStartcol(), column.getColumnName(), Const.META_TYPE_STRING, style,helper);
		}
	}
	private static void createCellRegion(Sheet targetsheet,Workbook wb,TableHeaderProp prop,TableMergeRegion region,Row[] headerRows,CreationHelper helper){
		CellStyle style=null;
		Row baseRow=headerRows[region.getStartrow()];
		if(region.getColheigth()>1 || region.getCollength()>1){
			style=ExcelBaseOper.getStyle(wb, 3, CellStyle.ALIGN_CENTER,prop);
			ExcelBaseOper.merged(targetsheet, baseRow,Const.META_TYPE_STRING, region.getStartrow(), region.getStartcol(), region.getStartrow()+region.getColheigth()-1, region.getStartcol()+region.getCollength()-1, style, region.getName(),helper);
		}
		else{
			style=ExcelBaseOper.getStyle(wb, 1, CellStyle.ALIGN_CENTER,prop);
			ExcelBaseOper.createCell(baseRow, region.getStartcol(), region.getName(), Const.META_TYPE_STRING, style,helper);
		}
	}
	private static void FillColumns(Workbook wb,Sheet targetsheet,ExcelSheetProp prop,TableHeaderProp header,CreationHelper helper) throws Exception
	{
		try{
			int headerrow=1;
			if(header!=null)
				headerrow=header.getContainrow();
			else
				throw new Exception("Excel Header is null");
		if(prop.getColumnPropList().size()!=0){
			List<Map<String, String>> list=prop.getColumnList();
			for(int i=0;i<list.size();i++){
				processSingleLine(list.get(i),wb,targetsheet,i+1,prop,header,helper);
			}
		}
		}catch (Exception e) {
			log.error(e);
			throw e;
			
		}
		
	}
	public static void processSingleLine(Map<String, ?> map,Workbook wb,Sheet targetsheet,int i,ExcelSheetProp prop,TableHeaderProp header,CreationHelper helper){
		{
			int startRow=prop.getStartRow()+header.getHeaderRows()-1;
			int startCol=prop.getStartCol()-1;
			int fieldCount=prop.getColumnPropList().size();
			List<Map<String, String>> list=prop.getColumnList();
			String[] valueArr=new String[fieldCount];
			int[] fromPos=new int[fieldCount];
			boolean [] shallMergin=new boolean[fieldCount];
			ExcelCellStyleUtil util=ExcelCellStyleUtil.getInstance();
			for(int pos=0;pos<fieldCount;pos++)
				fromPos[pos]=-1;
			Row row=ExcelBaseOper.creatRow(targetsheet, startRow+i);
			for(int j=0;j<prop.getColumnPropList().size();j++){
				ExcelColumnProp excelprop=prop.getColumnPropList().get(j);
				String columnCode=excelprop.getColumnCode();
				String columnType=excelprop.getColumnType();
				boolean needMerge=excelprop.isNeedMerge();
				String valueobj=map.get(columnCode).toString();
				if(valueobj==null)
					valueobj=map.get(columnCode.toUpperCase()).toString();
				if(valueobj==null)
					valueobj=map.get(columnCode.toLowerCase()).toString();
				CellStyle stylesingle=util.getCellStyle(wb, 1, 1, columnType, header);
				CellStyle stylemutil=util.getCellStyle(wb, 1, 2, columnType, header);
				if(needMerge){
					if(isFollowingSame(list, i, columnCode)){
						valueArr[j]=valueobj;
						if(fromPos[j]==-1)
							fromPos[j]=i;
					}else if(valueArr[j]!=null && !"".equalsIgnoreCase(valueArr[j].trim())){
						if(fromPos[j]!=-1){
							ExcelBaseOper.merged(targetsheet, ExcelBaseOper.getRow(targetsheet, fromPos[j]+startRow),columnType, fromPos[j]+startRow, startCol+j, i+startRow, startCol+j, stylemutil, valueobj, helper);
							fromPos[j]=-1;
							valueArr[j]="";
							shallMergin[j]=true;
						}else{
							ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
						}
					}else{
						ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
					}
				}else{
					ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
				}

			}
			for(int k=0;k<fieldCount;k++){
				ExcelColumnProp excelprop=prop.getColumnPropList().get(k);
				boolean needMerge=excelprop.isNeedMerge();
				String columnType=excelprop.getColumnType();
				CellStyle stylesingle=util.getCellStyle(wb, 1, 1, columnType, header);
				CellStyle stylemutil=util.getCellStyle(wb, 1, 2, columnType, header);
				if(needMerge){
					boolean shallParentMerge=shallMergin[k];
					if(shallParentMerge){
						for(int p=k;p<fieldCount;p++){
							boolean subMerge=prop.getColumnPropList().get(p).isNeedMerge();
							if(subMerge && valueArr[p]!=null && !"".equals(valueArr[p].trim()) && fromPos[p]!=-1){
								if(fromPos[p]!=i)
									ExcelBaseOper.merged(targetsheet, ExcelBaseOper.getRow(targetsheet, fromPos[p]+startRow),columnType, fromPos[p]+startRow, startCol+k, i+startRow, startCol+k, stylemutil, valueArr[p], helper);
								else
									ExcelBaseOper.createCell(ExcelBaseOper.getRow(targetsheet, fromPos[p]+startRow), p, valueArr[p], columnType, stylesingle, helper);
								fromPos[p]=-1;
								valueArr[p]="";
							}
						}
						shallMergin[k]=false;
						break;
					}

				}
			}
			if(prop.getColumnList()!=null) {
				if (i == prop.getColumnList().size() - 1) {
					for (int d = 0; d < prop.getColumnPropList().size(); d++) {
						boolean needMerge = prop.getColumnPropList().get(d).isNeedMerge();
						if (needMerge) {
							if (fromPos[d] != -1 && !"".equals(valueArr[d])) {
								CellStyle stylemutil = util.getCellStyle(wb, 1, 2, prop.getColumnPropList().get(d).getColumnType(), header);
								ExcelBaseOper.merged(targetsheet, ExcelBaseOper.getRow(targetsheet, fromPos[d] + startRow), prop.getColumnPropList().get(d).getColumnType(), fromPos[d] + startRow, startCol + i, i + startRow, startCol + i, stylemutil, valueArr[d], helper);
							} else {
								String columnCode = prop.getColumnPropList().get(d).getColumnCode();
								String columnType = prop.getColumnPropList().get(d).getColumnType();
								CellStyle stylesingle = util.getCellStyle(wb, 1, 1, columnType, header);
								String valueobj = map.get(columnCode).toString();
								if (valueobj == null)
									valueobj = map.get(columnCode.toUpperCase()).toString();
								if (valueobj == null)
									valueobj = map.get(columnCode.toLowerCase()).toString();
								ExcelBaseOper.createCell(ExcelBaseOper.getRow(targetsheet, startRow + i), d, valueobj, columnType, stylesingle, helper);
							}
						}
					}
				}
			}
		}
	}
	private static void autoSizeSheet(Sheet sheet,int count){
		for (int i = 0; i < count; i++) {
			sheet.autoSizeColumn(i);
		}
		
	}
	private static boolean isFollowingSame(List<Map<String, String>> resultList,int nowpos,String columnName){
		if(nowpos+1<resultList.size()){
			String value1=resultList.get(nowpos).get(columnName);
			String cmpvalue=resultList.get(nowpos+1).get(columnName);
			return cmpvalue.trim().equals(value1.trim());
		}
		return false;
	}
	private static void FillColumns(Workbook wb,Sheet targetsheet,ExcelSheetProp prop)
	{
		if(prop.getColumnList().size()!=0)
		{
			int i=0;
			Iterator<Map<String,String>> it=prop.getColumnList().iterator();
			ExcelCellStyleUtil cellUtil=ExcelCellStyleUtil.getInstance();
			while(it.hasNext())
			{
				Map<String,String> map=it.next();
				Row row1 = targetsheet.createRow(i+1);
				
				for (int j = 0; j < prop.getColumnName().length; j++) {
					String columname = prop.getColumnName()[j];
					
					Object valueobj=map.get(columname);
					if(valueobj==null)
						valueobj=map.get(columname.toUpperCase());
					if(valueobj==null)
						valueobj=map.get(columname.toLowerCase());
					String value="";
					if(valueobj!=null)
						value=valueobj.toString();
					String colType=prop.getColumnType()[j];
					
					if(columname!=null && !"".equals(columname))
					{
						HSSFCellStyle cellStyle =(HSSFCellStyle) cellUtil.getCellStyle(wb, 1, 1, colType, null);
						if(colType.equals(Const.META_TYPE_STRING))
							createCell(cellStyle,row1, (short) j, HSSFCellStyle.ALIGN_CENTER,value);
						else if(colType.equals(Const.META_TYPE_NUMERIC) || colType.equals(Const.META_TYPE_DOUBLE)){ 
						   if(!"".equals(value)){
								createCell(cellStyle, row1, (short) j, HSSFCellStyle.ALIGN_CENTER,Double.parseDouble(value));
						   }
						}else if(colType.equals(Const.META_TYPE_BIGINT)){
							createCell(cellStyle, row1, (short) j, HSSFCellStyle.ALIGN_CENTER,Long.parseLong(value));
						}else if(colType.equalsIgnoreCase(Const.META_TYPE_INTEGER)){
							createCell(cellStyle, row1, (short) j, HSSFCellStyle.ALIGN_CENTER,Integer.parseInt(value));
						}
						else if(colType.equals(Const.META_TYPE_DATE)){
							if(!"".equals(value))
								createCellDate(cellStyle, row1, (short) j, HSSFCellStyle.ALIGN_CENTER, value);
						}else
							createCell(cellStyle,row1, (short) j, HSSFCellStyle.ALIGN_CENTER,value);
						}		
					}
				i++;
			}	
				
		}
		
	}


	private static void createCell(CellStyle cellStyle,Row row, int column, short align,String objvalue)
    {
        Cell cell = row.createCell(column);
        //cellStyle.setAlignment(align);
        cell.setCellStyle(cellStyle);
        //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        String value="";
        if(objvalue!=null)
        	value=objvalue.toString();
        cell.setCellValue(value);
    }
	private static void createCell(CellStyle cellStyle, Row row, int column, short align,double value)
    {
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(value);
    }
	private static void createCellDate(CellStyle cellStyle, Row row, int column, short align,String value)
    {
        Cell cell = row.createCell(column);
        //cellStyle.setAlignment(align);
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd hh:mm:ss"));
        cell.setCellStyle(cellStyle);
        cell.setCellValue(value);
    }
	private static int[] getExcelPosition(String startPos,String endPos){
		int startPosLen=startPos.length();
		int endPosLen=endPos.length();
		int startRow=0;
		int startCol=0;
		int endRow=0;
		int endCol=0;
		for(int i=0;i<startPosLen;i++){
			if(isChar(startPos.charAt(i))){
				startCol=startRow*26+getDigintalByChar(startPos.toUpperCase().charAt(i));
			}else{
				startRow=Integer.parseInt(startPos.substring(i,startPos.length()));
				break;
			}
		}
		for(int j=0;j<endPosLen;j++){
			if(isChar(endPos.charAt(j))){
				endCol=endCol*26+getDigintalByChar(endPos.toUpperCase().charAt(j));
			}else{
				endRow=Integer.parseInt(endPos.substring(j,endPos.length()));
				break;
			}
		}
		return new int[]{startCol,endCol,startRow,endRow};
	}
	private static boolean isChar(char str){
		return Pattern.matches("[A-Z]", String.valueOf(str));
		
	}
	public static boolean isValidExcelInput(String str){
		return Pattern.matches("[A-Z]+[0-9]+", str);
	}
	private static int getDigintalByChar(char str){
		int startChar=(int) 'A';
		return (int) str-startChar+1;
	}
	
}
