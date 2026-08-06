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
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ExcelBaseOper {

    private ExcelBaseOper() {

    }

    public static final String TYPE_EXCEL2003 = "xls";
    public static final String TYPE_EXCEL2007 = "xlsx";
    public static final String defaultFontName = Locale.CHINA.equals(Locale.getDefault()) || Locale.SIMPLIFIED_CHINESE.equals(Locale.getDefault()) ? "宋体" : "Calibri";
    private static final Logger logger = LoggerFactory.getLogger(ExcelBaseOper.class);
    private static final Pattern paramPattern=Pattern.compile("\\w+(\\{P([\\+|-]?[\\d+])?\\})");


    public static Workbook createWorkBook(ExcelSheetProp prop) {
        boolean is2007 = ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileExt());
        Workbook wb;

        if (!is2007) {
            wb = Optional.ofNullable(prop.getTemplateFile()).map(f -> {
                        try {
                            return new HSSFWorkbook(getTemplateInputStream(prop));
                        } catch (IOException ex) {
                            return null;
                        }
                    })
                    .orElseGet(HSSFWorkbook::new);
        } else {
            wb = Optional.of(prop.isStreamMode()).map(f -> {
                Workbook wb1 = Optional.ofNullable(prop.getTemplateFile()).map(p1 -> {
                    try {
                        SXSSFWorkbook wb3 = new SXSSFWorkbook(new XSSFWorkbook(getTemplateInputStream(prop)), prop.getStreamRows());
                        wb3.setCompressTempFiles(true);
                        return wb3;
                    } catch (IOException ex) {
                        log.error("{}", ex.getMessage());
                        return null;
                    }
                }).orElseGet(() -> {
                    SXSSFWorkbook tmpwb = new SXSSFWorkbook(prop.getStreamRows());
                    tmpwb.setCompressTempFiles(true);
                    return tmpwb;
                });
                return wb1;
            }).orElseGet(() -> {
                Workbook wb2 = Optional.ofNullable(prop.getTemplateFile()).map(p -> {
                    try {
                        return new XSSFWorkbook(getTemplateInputStream(prop));
                    } catch (IOException ex) {
                        log.error("{}", ex.getMessage());
                        return null;
                    }
                }).orElseGet(XSSFWorkbook::new);
                return wb2;
            });
        }
        return wb;
    }

    private static InputStream getTemplateInputStream(ExcelSheetProp prop) throws IOException {
        Assert.notNull(prop.getTemplateFile(), "");
        if (prop.getTemplateFile().startsWith("classpath:")) {
            return ExcelProcessor.class.getClassLoader().getResourceAsStream(prop.getTemplateFile().substring(10));
        } else {
            return Files.newInputStream(Paths.get(prop.getTemplateFile()));
        }
    }

    public static Workbook createWorkBook(String filePrefix, InputStream stream) throws IOException {
        Workbook wb = null;
        if (TYPE_EXCEL2003.equalsIgnoreCase(filePrefix)) {
            wb = new HSSFWorkbook(stream);
        } else if (TYPE_EXCEL2007.equalsIgnoreCase(filePrefix)) {
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
            cs.setFillForegroundColor(new XSSFColor(new Color(rgb[0], rgb[1], rgb[2]), new DefaultIndexedColorMap()).getIndexed());
        } else {
            cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }

        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cs;
    }

    private static void setHeaderFont(Workbook wb, TableConfigProp header, CellStyle cs) {
        Font font = wb.createFont();
        font.setFontName(defaultFontName);
        if (header != null) {
            font.setFontName((header.getHeaderFontName() == null || header.getHeaderFontName().isEmpty()) ? defaultFontName : header.getHeaderFontName());
            if (header.isBold()) {
                font.setBold(true);
            }
            if (header.isItalic()) {
                font.setItalic(true);
            }
        }
        cs.setFont(font);
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

    public static Cell merged(Sheet sheet, Const.CLASSTYPE colType, int startRow, int startCell, int endRow, int endCell, CellStyle cs, String value, CreationHelper helper) {
        Cell cell;
        CellRangeAddress cellRangeAddress = new CellRangeAddress(startRow, endRow, startCell, endCell);
        sheet.addMergedRegion(cellRangeAddress);
        setRegionStyle(sheet, cellRangeAddress, cs);
        Row row1 = getRow(sheet, startRow);
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

    public static Row creatRow(Sheet sheet, int i) {
        return sheet.createRow(i);
    }

    public static Row getRow(Sheet sheet, int i) {
        Row row = sheet.getRow(i);
        if (row == null) {
            row = sheet.createRow(i);
        }
        return row;
    }

    private static short getAlignment(int align) {
        return (short) align;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, String objvalue) {
        Cell cell = row.createCell(column);

        cell.setCellValue(objvalue);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    public static Cell createCell(Row row1, int j, Object value, Const.CLASSTYPE colType, CellStyle cellStyle, CreationHelper helper) {
        Cell cell;

        if (colType.equals(Const.CLASSTYPE.STRING)) {
            cell = createCell(row1, j, cellStyle, helper, value.toString());
        } else if (colType.equals(Const.CLASSTYPE.NUMERIC) || colType.equals(Const.CLASSTYPE.DOUBLE)) {
            if (!ObjectUtils.isEmpty(value)) {
                cell = createCell(row1, j, cellStyle, helper, Double.parseDouble(value.toString()));
            } else {
                cell = createCell(row1, j, cellStyle, helper, "");
            }
        } else if (colType.equals(Const.CLASSTYPE.INTEGER)) {
            if (!ObjectUtils.isEmpty(value)) {
                cell = createCell(row1, j, cellStyle, helper, Integer.parseInt(value.toString()));
            } else {
                cell = createCell(row1, j, cellStyle, helper, "");
            }
        } else if (colType.equals(Const.CLASSTYPE.DATE) || colType.equals(Const.CLASSTYPE.TIMESTAMP)) {
            if (!ObjectUtils.isEmpty(value)) {
                Long timeTs=0L;
                if(value.getClass().equals(Long.class)){
                    timeTs=Long.valueOf(value.toString());
                }else if(value.getClass().equals(LocalDateTime.class)){
                    LocalDateTime dateTime=(LocalDateTime) value;
                    timeTs=dateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(dateTime)).toEpochMilli();
                }else if(value.getClass().equals(Timestamp.class)){
                    Timestamp timestamp=(Timestamp) value;
                    timeTs=timestamp.getTime();
                }else if(value.getClass().equals(java.util.Date.class)){
                    java.util.Date date=(java.util.Date)value;
                    timeTs=date.getTime();
                }else if(value.getClass().equals(java.sql.Date.class)){
                    java.sql.Date date=(java.sql.Date)value;
                    timeTs=date.getTime();
                }
                if(timeTs>0L) {
                    double dateVal=DateUtil.getExcelDate(new Timestamp(timeTs));
                    cell = createCellDate(row1, j, cellStyle, helper, dateVal);
                }else{
                    cell = createCell(row1, j, cellStyle, helper, "");
                }
            } else {
                cell = createCell(row1, j, cellStyle, helper, "");
            }
        } else if (colType.equals(Const.CLASSTYPE.FORMULA)) {
            cell = createFormulaCell(row1, j, cellStyle, value.toString());
        } else {
            cell = createCell(row1, j, cellStyle, helper, value.toString());
        }
        return cell;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, double value) {
        Cell cell = row.createCell(column);

        cellStyle.setDataFormat(helper.createDataFormat().getFormat("0.00"));

        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private static Cell createFormulaCell(Row row, int column, CellStyle cellStyle, String formula) {
        Cell cell = row.createCell(column);
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        cell.setCellFormula(formula);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(evaluator.evaluate(cell).getNumberValue());
        return cell;
    }

    private static Cell createCell(Row row, int column, CellStyle cellStyle, CreationHelper helper, int value) {
        Cell cell = row.createCell(column);

        cellStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));

        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private static Cell createCellDate(Row row, int column, CellStyle cellStyle, CreationHelper helper, double value) {
        Cell cell = row.createCell(column);
        //cellStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    public static ExcelHeaderProp getHeaderPropFromJson(String jsonStr) {
        ExcelHeaderProp prop = new ExcelHeaderProp();
        JSONArray array = JSONArray.fromObject(jsonStr);
        JsonConfig config = new JsonConfig();
        config.setRootClass(ExcelMergeRegion.class);
        Map<String, Class> map = new HashMap<>();
        map.put("subRegions", ExcelMergeRegion.class);
        config.setClassMap(map);
        Collection<ExcelMergeRegion> col = JSONArray.toCollection(array, config);
        prop.setHeaderList((List) col);
        return prop;
    }

    public static List<Triple<Integer, Integer, List<Object>>> getMergedCells(Sheet sheet, int startRow, int mergeColumn, int[] additionSingleColumns) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        List<Triple<Integer, Integer, List<Object>>> retList = new ArrayList<>();

        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (firstRow >= startRow) {
                if (firstColumn <= mergeColumn && lastColumn >= mergeColumn) {
                    Row row = sheet.getRow(firstRow);
                    Object value = getCellValue(row.getCell(firstColumn));
                    List<Object> rList1 = new ArrayList<>(Arrays.asList(value));
                    if (additionSingleColumns != null) {
                        for (Integer col : additionSingleColumns) {
                            rList1.add(getCellValue(row.getCell(col)));
                        }
                    }
                    retList.add(Triple.of(firstRow, lastRow, rList1));
                }
            }
        }
        if (!CollectionUtils.isEmpty(retList)) {
            return retList.stream().sorted(Comparator.comparing(Triple::getLeft)).collect(Collectors.toList());
        }
        return retList;
    }

    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object cellValue;
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        switch (cell.getCellType()) {
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = evaluator.evaluate(cell).getStringValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    double d = cell.getNumericCellValue();
                    Date date = DateUtil.getJavaDate(d);
                    cellValue = new Timestamp(date.getTime());
                } else {
                    cellValue = cell.getNumericCellValue();
                }
                break;
            default:
                cellValue = cell.getStringCellValue();
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

        } catch (Exception e) {
            logger.error("Encounter Error ", e);
        }
        return colorset;
    }
    public final static String returnFormulaWithPos(String formula,int linePos){
        Matcher matcher=paramPattern.matcher(formula);
        StringBuffer buffer=new StringBuffer();
        while(matcher.find()){
            String groupStr=matcher.group();
            int pos=groupStr.indexOf("{P");
            String columnName=groupStr.substring(0,pos);
            Integer stepNum=linePos;
            if(pos+3<groupStr.length()) {
                String addPlustag = groupStr.substring(pos+2 , pos+3);
                stepNum = "+".equals(addPlustag) ? stepNum + Integer.valueOf(groupStr.substring(pos + 3, groupStr.length()-1)) : stepNum - Integer.valueOf(groupStr.substring(pos + 3, groupStr.length()-1));
            }
            matcher.appendReplacement(buffer,columnName+stepNum);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
