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
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelBaseOper {
    public static final String TYPE_EXCEL2003 = "xls";
    public static final String TYPE_EXCEL2007 = "xlsx";
    private static final String defaultFontName = java.awt.Font.SANS_SERIF;
    private static final Logger logger = LoggerFactory.getLogger(ExcelBaseOper.class);

    public static Sheet createSheet(Workbook wb, String sheetName, ExcelSheetProp prop) {
        Sheet sheet = null;
        String fileext = prop.getFileExt();
        if (TYPE_EXCEL2003.equalsIgnoreCase(fileext)) {
            sheet = wb.createSheet(sheetName);
        } else if (TYPE_EXCEL2007.equalsIgnoreCase(fileext)) {
            sheet = wb.createSheet(sheetName);
        }
        return sheet;
    }
    public static Workbook createWorkBook(ExcelSheetProp prop){
        return createWorkBook(prop.getFileExt(),prop.isStreamInsert(),prop.getStreamRows());
    }

    public static Workbook createWorkBook(String fileExt,boolean isStreamInsert,int streamRows) {
        Workbook wb = null;

        if (TYPE_EXCEL2003.equalsIgnoreCase(fileExt)) {
            wb = new HSSFWorkbook();
        } else if (TYPE_EXCEL2007.equalsIgnoreCase(fileExt)) {
            if (!isStreamInsert) {
                wb = new XSSFWorkbook();
            } else {
                wb = new SXSSFWorkbook(streamRows);
                ((SXSSFWorkbook) wb).setCompressTempFiles(true);
            }
        }
        return wb;
    }

    public static Workbook getWorkBook(ExcelSheetProp prop, InputStream stream) throws IOException {
        Workbook wb = null;
        String fileext = prop.getFileExt();
        if (TYPE_EXCEL2003.equalsIgnoreCase(fileext)) {
            wb = new HSSFWorkbook(stream);
        } else if (TYPE_EXCEL2007.equalsIgnoreCase(fileext)) {
            wb = new XSSFWorkbook(stream);
        }
        return wb;
    }

    public static CellStyle getHeaderStyle(Workbook wb, int rowspan, HorizontalAlignment align, TableMergeRegion region, TableConfigProp header) {
        CellStyle cs = wb.createCellStyle();

        if (rowspan > 1) {
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        cs.setAlignment(align);
        cs.setBorderLeft(BorderStyle.THIN);

        cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderRight(BorderStyle.THIN);

        cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderTop(BorderStyle.THIN);

        cs.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        setHeaderFont(wb, header, cs);

        String color = region.getForegroundcolor();
        if (color != null && !"".equals(color.trim())) {
            int[] rgb = hex2rgb(color);
            cs.setFillForegroundColor(new XSSFColor(new Color(rgb[0], rgb[1], rgb[2]),new DefaultIndexedColorMap()).getIndexed());
        } else {
            cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }

        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cs;
    }

    private static void setHeaderFont(Workbook wb, TableConfigProp header, CellStyle cs) {
        if (header != null) {
            Font font = wb.createFont();
            font.setFontName((header.getHeaderFontName() == null || header.getHeaderFontName().isEmpty()) ? defaultFontName : header.getHeaderFontName());
            if (header.isBold()) {
                font.setBold(true);
            }
            if (header.isItalic()) {
                font.setItalic(true);
            }
            cs.setFont(font);
        }
    }

    public static CellStyle getHeaderStyle(Workbook wb, int rowspan, HorizontalAlignment align, TableConfigProp header) {
        CellStyle cs = wb.createCellStyle();

        //if (rowspan > 1) {
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        //}
        cs.setAlignment(align);

        cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderLeft(BorderStyle.THIN);

        cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);

        cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderTop(BorderStyle.THIN);

        cs.setTopBorderColor(IndexedColors.BLACK.getIndex());

        cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        setHeaderFont(wb, header, cs);
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setWrapText(true);
        return cs;
    }

    public static Cell merged(Sheet sheet, String colType, int startRow, int startCell, int endRow, int endCell, CellStyle cs, String value, CreationHelper helper) {
        Cell cell = null;
        CellRangeAddress cellRangeAddress = new CellRangeAddress(startRow, endRow,  startCell, endCell);
        sheet.addMergedRegion(cellRangeAddress);
        setRegionStyle(sheet, cellRangeAddress, cs);
        Row row1=getRow(sheet,startRow);
        cell = createCell(row1, startCell, value, colType, cs, helper);
        return cell;
    }

    private static void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress, CellStyle cs) {
        for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress.getLastRow(); i++) {
            Row row = getRow(sheet, i);
            for (int j = cellRangeAddress.getFirstColumn(); j <= cellRangeAddress.getLastColumn(); j++) {
                Cell cell = CellUtil.getCell(row, (short) j);
                cell.setCellStyle(cs);
            }
        }
    }



    private static void caculateStartColofRow(int row, int pos, ExcelHeaderProp header, int[][] startColArr) {
        if (row == 0) {
            if (pos == 0) {
                startColArr[0][0] = 0;
            } else {
                startColArr[0][pos] = startColArr[0][pos - 1]
                        + header.getHeaderColumnList().get(0).get(pos - 1)
                        .getColspan();
            }
        } else {
            int count = header.getHeaderColumnList().size();
            if (pos == 0) {
                int posnum = 0;
                int poscount = 0;
                int posfix = pos + 1;
                for (int i = 0; i < row; i++) {
                    List<ExcelHeaderColumn> list = header.getHeaderColumnList().get(i);
                    for (int j = 0; j < list.size(); j++) {
                        ExcelHeaderColumn column = list.get(j);
                        int rowspan = column.getRowspan();
                        if (rowspan + i != count) {
                            poscount++;
                        }
                        if (poscount == posfix) {
                            startColArr[row][pos] = startColArr[i][j];
                            return;
                        }
                    }
                }
            } else {
                List<ExcelHeaderColumn> listabove = header.getHeaderColumnList().get(row - 1);
                List<ExcelHeaderColumn> list = header.getHeaderColumnList().get(row);
                int nums = 0;
                int totallength = 0;
                int[] collength = new int[pos];
                for (int i = 0; i < pos; i++) {
                    totallength += list.get(i).getColspan();
                    collength[i] = totallength;
                }

                for (int i = 0; i < listabove.size(); i++) {
                    ExcelHeaderColumn column = listabove.get(i);
                    int rowspan = column.getRowspan();
                    int colspan = column.getColspan();
                    if (rowspan + row - 1 != count) {
                        nums += colspan;
                    }

                    if (nums > totallength) {
                        if (isColumnTheFristChild(collength, listabove, i, row, count)) {
                            startColArr[row][pos] = startColArr[row - 1][i];

                        } else {

                            startColArr[row][pos] = startColArr[row][pos - 1] + header.getHeaderColumnList().get(row).get(pos - 1).getColspan();
                        }
                        break;

                    } else if (nums == totallength && i < listabove.size() - 1) {

                        int temp1 = i + 1;
                        while (header.getHeaderColumnList().get(row - 1).get(temp1).getRowspan() + row - 1 == count) {
                            temp1++;
                        }
                        startColArr[row][pos] = startColArr[row - 1][temp1];
                        break;
                    }
                }
                if (startColArr[row][pos] == 0) {
                    startColArr[row][pos] = startColArr[row][pos - 1] + header.getHeaderColumnList().get(row).get(pos - 1).getColspan();
                }
            }
        }

    }

    private static boolean isColumnTheFristChild(int[] collengtharr, List<ExcelHeaderColumn> listabove, int abovecol, int aboverow, int rowcount) {
        int nums = 0;
        boolean isfrist = false;
        int beforemaxnums = collengtharr[collengtharr.length - 1];
        for (int i = 0; i < abovecol; i++) {
            ExcelHeaderColumn column = listabove.get(i);
            int rowspan = column.getRowspan();
            int colspan = column.getColspan();
            if (rowspan + aboverow - 1 != rowcount) {
                nums += colspan;
            }
        }
        if (nums == beforemaxnums) {
            isfrist = true;
        }
        return isfrist;

    }


    public static Row creatRow(Sheet sheet, int i) {
        return sheet.createRow(i);
    }

    public static Row getRow(Sheet sheet, int i) {
        Row row=sheet.getRow(i);
        if(row==null){
            row=sheet.createRow(i);
        }
        return row;
    }

    private static Cell creatCell(Row row, int i) {
        return row.createCell(i);
    }

    private static short getAlignment(int align) {
        return (short) align;
    }

    public static Cell createCell(Row row1, int j, String value, String colType, CellStyle cellStyle, CreationHelper helper) {
        Cell cell = null;
        if (colType.equals(Const.META_TYPE_STRING)) {
            cell = createCell(row1, j, cellStyle, helper, value);
        } else if (colType.equals(Const.META_TYPE_NUMERIC) || colType.equals(Const.META_TYPE_DOUBLE)) {
            if (!StringUtils.isEmpty(value)) {
                cell = createCell(row1, j, cellStyle, helper, Double.parseDouble(value));
            }else{
                cell=createCell(row1,j,cellStyle,helper,"");
            }
        } else if (colType.equals(Const.META_TYPE_INTEGER)) {
            if (!StringUtils.isEmpty(value)) {
                cell = createCell(row1, j, cellStyle, helper, Integer.parseInt(value));
            }else {
                cell=createCell(row1,j,cellStyle,helper,"");
            }
        } else if (colType.equals(Const.META_TYPE_DATE) || colType.equals(Const.META_TYPE_TIMESTAMP)) {
            if (!StringUtils.isEmpty(value)) {
                cell = createCellDate(row1, j, cellStyle, helper, value);
            }else{
                cell=createCell(row1,j,cellStyle,helper,"");
            }
        }else if(colType.equalsIgnoreCase(Const.META_TYPE_FORMULA)){
            if(!StringUtils.isEmpty(value)){
                cell=createFormulaCell(row1,j,cellStyle,helper,value);
            }else{
                cell=createCell(row1,j,cellStyle,helper,"");
            }
        }
        else {
            cell = createCell(row1, j, cellStyle, helper, value);
        }
        return cell;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, String objvalue) {
        Cell cell = row.createCell(column);

        cell.setCellValue(objvalue);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, double value) {
        Cell cell = row.createCell(column);

        cellStyle.setDataFormat(helper.createDataFormat().getFormat("0.00"));

        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return cell;
    }
    private static Cell createFormulaCell(Row row,int column,CellStyle cellStyle,CreationHelper helper,String formula){
        Cell cell = row.createCell(column);
        cell.setCellType(CellType.FORMULA);
        cell.setCellFormula(formula);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, int value) {
        Cell cell = row.createCell(column);

        cellStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));

        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private static Cell createCellDate(Row row, int column, CellStyle cellStyle, CreationHelper helper, String value) {
        Cell cell = row.createCell(column);
        cellStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));

        cell.setCellValue(new Date(Long.parseLong(value)));
        cell.setCellStyle(cellStyle);
        return cell;
    }

    public static ExcelHeaderProp getHeaderPropFromJson(String jsonStr) {
        ExcelHeaderProp prop = new ExcelHeaderProp();
        JSONArray array = JSONArray.fromObject(jsonStr);
        JsonConfig config = new JsonConfig();
        config.setRootClass(ExcelMergeRegion.class);
        Map map = new HashMap();
        map.put("subRegions", ExcelMergeRegion.class);
        config.setClassMap(map);
        Collection<ExcelMergeRegion> col = JSONArray.toCollection(array, config);
        prop.setHeaderList((List) col);
        return prop;
    }
    public static List<Triple<Integer,Integer,List<Object>>> getMergedCells(Sheet sheet, int startRow, int mergeColumn,int[] additionSingleColumns) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        List<Triple<Integer,Integer,List<Object>>> retList=new ArrayList<>();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if(firstRow>=startRow){
                if(firstColumn<=mergeColumn && lastColumn>=mergeColumn){
                    Row row=sheet.getRow(firstRow);
                    Object value=getCellValue(row.getCell(firstColumn));
                    List<Object> rList1=new ArrayList<>(Arrays.asList(value));
                    if(additionSingleColumns!=null){
                        for(Integer col:additionSingleColumns){
                            rList1.add(getCellValue(row.getCell(col)));
                        }
                    }
                    retList.add(Triple.of(firstRow,lastRow,rList1));
                }
            }
        }
        if(!CollectionUtils.isEmpty(retList)){
           return retList.stream().sorted(Comparator.comparing(Triple::getLeft)).collect(Collectors.toList());
        }
        return retList;
    }
    public static Object getCellValue(Cell cell){
        if(cell==null){
            return null;
        }
        Object cellValue=null;
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        switch (cell.getCellType()){
            case STRING:
                cellValue=cell.getStringCellValue();
                break;
            case BOOLEAN:
                cellValue=String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue=evaluator.evaluate(cell).getStringValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)){
                    double d = cell.getNumericCellValue();
                    Date date = DateUtil.getJavaDate(d);
                    cellValue = new Timestamp(date.getTime());
                }else{
                    cellValue = cell.getNumericCellValue();
                }
                break;
        }
        return cellValue;
    }
    private static int[] hex2rgb(String colorstr) {
        int[] colorset = new int[3];
        String sr, sg, sb, sHex;
        if (colorstr.length() != 6) {
            colorstr = "D6D3CE";
        }
        sHex = colorstr.toUpperCase();
        sr = sHex.substring(0, 2);
        sg = sHex.substring(2, 4);
        sb = sHex.substring(4, 6);
        try {
            // Convert Hex to Dec
            colorset[0] = Integer.parseInt(sr, 16);
            colorset[1] = Integer.parseInt(sg, 16);
            colorset[2] = Integer.parseInt(sb, 16);
            for (int i = 0; i < colorset.length; i++) {
                if (colorset[i] > 255) {
                    colorset[i] = 255;
                }
            }
            System.out.println(" using color" + colorset[0] + "," + colorset[1] + "," + colorset[2]);
        } catch (Exception e) {
            logger.error("Encounter Error ", e);
        }
        return colorset;
    }

}
