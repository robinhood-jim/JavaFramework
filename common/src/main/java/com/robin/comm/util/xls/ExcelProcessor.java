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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class ExcelProcessor {
    private static final DateTimeFormatter localformat = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static void readExcelFile(String filename, ExcelSheetProp prop) throws FileNotFoundException {
        try (InputStream myxls = Files.newInputStream(Paths.get(filename))) {
            readExcelFile(myxls, prop, null, null);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new FileNotFoundException("Read Error!");
        }
    }


    public static void readExcelFile(InputStream myxls, ExcelSheetProp prop, IExcelAfterProcessor processor, DateTimeFormatter formatter) throws IOException {
        boolean is2007 = ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileExt());
        Workbook wb;
        if (is2007) {
            wb = new XSSFWorkbook(myxls);
        } else {
            wb = new HSSFWorkbook(myxls);
        }
        readExcelFile(wb, prop, processor, formatter);
    }

    public static void readExcelFile(Workbook wb, ExcelSheetProp prop, IExcelAfterProcessor processor, DateTimeFormatter formatter) {
        Sheet sheet = wb.getSheetAt(prop.getSheetNum());
        List<Map<String, Object>> columnValueList = new ArrayList<>();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        //hh24:mi:ss
        int pos = 0;
        for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext(); ) {
            pos++;
            if (pos <= prop.getStartRow()) {
                rit.next();
                continue;
            }
            Map<String, Object> listMap = new HashMap<>();
            Row row = rit.next();

            int j = 0;
            int endpos = prop.getStartCol() + prop.getColumnPropList().size() - 1;
            boolean ishasrecord = false;
            for (int i = prop.getStartCol() - 1; i < endpos; i++) {
                if (i >= prop.getColumnPropList().size()) {
                    break;
                }
                String type = prop.getColumnPropList().get(i).getColumnType();

                Cell cell = row.getCell(i);
                Object strCell = "";

                if (cell != null) {
                    strCell = readValue(cell, type, formatter == null ? localformat : formatter, evaluator);
                }
                if (!Objects.isNull(strCell)) {
                    ishasrecord = true;
                }
                listMap.put(prop.getColumnPropList().get(j).getColumnCode(), strCell);
                j++;
            }
            if (ishasrecord) {
                if (processor != null) {
                    processor.processLine(listMap);
                } else {
                    columnValueList.add(listMap);
                }
            }
        }
        prop.setColumnList(columnValueList);
    }

    public static int readExcelFile(InputStream myxls, int sheetIndex, Map<String, DataTypeEnum> columnMap, String startPos, String endPos, ExcelSheetProp prop, DateTimeFormatter format) {
        int pos = 0;
        List<Map<String, Object>> columnValueList = new ArrayList<>();
        boolean is2007 = ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(prop.getFileExt());
        try (Workbook wb = is2007 ? new XSSFWorkbook(myxls) : new HSSFWorkbook(myxls)) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            List<DataTypeEnum> columnList = new ArrayList<>();
            List<Integer> collist = new ArrayList<>();
            int[] excelPos = getExcelPosition(startPos, endPos);
            log.debug("{} {}", excelPos[2], excelPos[3]);
            int startCol = excelPos[0];
            int endCol = excelPos[1];
            int startRow = excelPos[2];
            int endRow = excelPos[3];
            for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext(); ) {
                pos++;
                Row row = rit.next();
                if (pos == startRow) {
                    for (int i = startCol - 1; i < endCol; i++) {
                        Cell cell = row.getCell(i);
                        String cellName = cell.getStringCellValue();
                        DataTypeEnum column = columnMap.get(cellName.toUpperCase());
                        if (column == null) {
                            column = columnMap.get(cellName.toLowerCase());
                        }
                        if (column != null) {
                            collist.add(i);
                            columnList.add(column);
                        }
                    }
                    continue;
                } else if (pos < startRow) {
                    continue;
                } else if (pos > endRow) {
                    break;
                }
                Map<String, Object> listMap = new HashMap<>();

                boolean ishasrecord = false;


                for (int j = 0; j < collist.size(); j++) {
                    int colpos = collist.get(j);
                    DataTypeEnum column = columnList.get(j);
                    String type = column.getDataType();
                    HSSFCell cell = (HSSFCell) row.getCell(colpos);
                    Object strCell = "";
                    if (cell != null) {
                        strCell = readValue(cell, type, format, evaluator);
                    }
                    if (!StringUtils.isEmpty(strCell)) {
                        ishasrecord = true;
                    }
                    listMap.put(column.getName(), strCell);
                }

                if (ishasrecord) {
                    columnValueList.add(listMap);
                }
            }
        } catch (Exception e) {
            log.error("encounter error!pos={} column size={}", pos, columnValueList.size());
            log.error("{}", e.getMessage());
        }

        prop.setColumnList(columnValueList);
        return pos;
    }

    public static Object readValue(Cell cell, String type, DateTimeFormatter format, FormulaEvaluator evaluator) {
        Object strCell;
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell) || type.equals(Const.META_TYPE_DATE) || type.equals(Const.META_TYPE_TIMESTAMP)) {
                        double d = cell.getNumericCellValue();
                        Date date = DateUtil.getJavaDate(d);
                        strCell = new Timestamp(date.getTime());
                    } else {
                        double d = cell.getNumericCellValue();
                        switch (type) {
                            case Const.META_TYPE_INTEGER:
                                strCell = Double.valueOf(d).intValue();
                                break;
                            case Const.META_TYPE_BIGINT:
                                strCell = Double.valueOf(d).longValue();
                                break;
                            case Const.META_TYPE_FLOAT:
                                strCell = Double.valueOf(d).floatValue();
                                break;
                            case Const.META_TYPE_DOUBLE:
                            case Const.META_TYPE_DECIMAL:
                            case Const.META_TYPE_NUMERIC:
                                strCell = d;
                                break;
                            default:
                                strCell = Double.toString(d);
                                break;
                        }
                    }
                    break;
                case STRING:
                    String str = cell.getStringCellValue();
                    switch (type) {
                        case Const.META_TYPE_INTEGER:
                            if (!StringUtils.isEmpty(str)) {
                                Double d = Double.valueOf(str);
                                strCell = d.intValue();
                            } else {
                                strCell = 0.0;
                            }
                            break;
                        case Const.META_TYPE_BIGINT:
                            if (!StringUtils.isEmpty(str)) {
                                strCell = Double.valueOf(str).longValue();
                            } else {
                                strCell = 0L;
                            }
                            break;
                        case Const.META_TYPE_FLOAT:
                            if (!StringUtils.isEmpty(str)) {
                                Double d = Double.valueOf(str);
                                strCell = d.floatValue();
                            } else {
                                strCell = Float.valueOf("0");
                            }
                            break;
                        case Const.META_TYPE_DOUBLE:
                        case Const.META_TYPE_DECIMAL:
                        case Const.META_TYPE_NUMERIC:
                            if (!StringUtils.isEmpty(str)) {
                                double d = Double.parseDouble(str);
                                strCell = (float) d;
                            } else {
                                strCell = 0.0;
                            }
                            break;
                        case Const.META_TYPE_DATE:
                        case Const.META_TYPE_TIMESTAMP:
                            if (!StringUtils.isEmpty(str)) {
                                LocalDateTime dateTime = LocalDateTime.parse(str, format);
                                strCell = Timestamp.valueOf(dateTime);
                            } else {
                                strCell = null;
                            }
                            break;
                        default:
                            strCell = cell.getStringCellValue();
                    }
                    break;
                case BOOLEAN:
                    strCell = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    CellValue formulaValue = evaluator.evaluate(cell);
                    CellType type1=formulaValue.getCellType();
                    if(CellType.NUMERIC.equals(type1)){
                        strCell=formulaValue.getNumberValue();
                    }else{
                        strCell=formulaValue.getStringValue();
                    }
                    break;
                default:
                    strCell = getDefaultValue(type);
                    break;
            }
        } else {
            strCell = getDefaultValue(type);
        }
        return strCell;
    }

    private static Object getDefaultValue(String type) {
        Object strCell;
        switch (type) {
            case Const.META_TYPE_INTEGER:
                strCell = 0;
                break;
            case Const.META_TYPE_BIGINT:
                strCell = 0L;
                break;
            case Const.META_TYPE_FLOAT:
                strCell = Float.valueOf("0");
                break;
            case Const.META_TYPE_DATE:
            case Const.META_TYPE_TIMESTAMP:
                strCell = null;
                break;
            case Const.META_TYPE_DOUBLE:
            case Const.META_TYPE_DECIMAL:
            case Const.META_TYPE_NUMERIC:
                strCell = 0.0;
                break;
            default:
                strCell = "";
                break;
        }
        return strCell;
    }

    public static void readExcel(InputStream stream, String filePrefix, int sheetIndex, int startRow, IExcelReadProcessor processor) throws IOException {
        boolean is2007 = ExcelBaseOper.TYPE_EXCEL2007.equalsIgnoreCase(filePrefix);
        try (Workbook wb = is2007 ? new XSSFWorkbook(stream) : new HSSFWorkbook(stream)) {
            readExcel(wb, sheetIndex, startRow, processor);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static void readExcel(Workbook wb,int sheetIndex, int startRow, IExcelReadProcessor processor) {
        try {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            int pos = 0;
            for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext(); ) {
                pos++;
                Row row = rit.next();
                if (pos > startRow) {
                    processor.doRead(wb, row, evaluator);
                }
            }

        } catch (Exception ex) {
            throw ex;
        }
    }


    public int getAvaiableRows(InputStream myxls, int startRow) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook(myxls);
        HSSFSheet sheet = wb.getSheetAt(0);
        int num = sheet.getLastRowNum();
        return num - startRow;
    }

    /**
     * Xls For 2003,xlsx for 2007
     *
     * @param prop
     * @return
     */

    public static Workbook generateExcelFile(ExcelSheetProp prop) {
        Workbook wb = ExcelBaseOper.createWorkBook(prop);
        String sheetname = prop.getSheetName();
        Sheet sheet = wb.createSheet(sheetname);
        if(ObjectUtils.isEmpty(prop.getTemplateFile())) {
            generateHeader(sheet, wb, prop);
        }
        fillColumns(wb, sheet, prop);
        autoSizeSheet(prop, sheet, prop.getColumnPropList().size());
        return wb;
    }



    public static void fillSheet(Sheet sheet, ExcelSheetProp prop, TableConfigProp configProp) throws IOException {
        if (prop.isFillHeader()) {
            if (configProp == null) {
                generateHeader(sheet, sheet.getWorkbook(), prop);
                fillColumns(sheet.getWorkbook(), sheet, prop);
            } else {
                generateHeader(sheet, sheet.getWorkbook(), prop, configProp, sheet.getWorkbook().getCreationHelper());
                fillColumns(sheet.getWorkbook(), sheet, prop, configProp, sheet.getWorkbook().getCreationHelper());
            }
        }
        autoSizeSheet(prop, sheet, prop.getColumnPropList().size());
    }

    public static void fillRow(Sheet sheet, ExcelSheetProp prop) {
        fillColumns(sheet.getWorkbook(), sheet, prop);
    }

    public static void createHeader(Sheet sheet, ExcelSheetProp prop, CellStyle style) {
        Row startRow = sheet.createRow(prop.getStartRow());
        for (int i = prop.getStartCol(); i < prop.getStartCol() + prop.getHeaderName().length; i++) {
            createCell(style, startRow, i, prop.getHeaderName()[i]);
        }
    }

    public static void createRows(Row row, ExcelSheetProp prop, List<Pair<String, Object>> values, CellStyle style) {
        for (int i = prop.getStartCol(); i < prop.getStartCol() + values.size(); i++) {
            createCell(values.get(i).getKey(), style, row, i, values.get(i).getValue().toString());
        }
    }

    /**
     * @param prop
     * @param header
     * @return
     */
    public static Workbook generateExcelFile(ExcelSheetProp prop, TableConfigProp header) throws Exception {
        Workbook wb = ExcelBaseOper.createWorkBook(prop);
        String sheetname = prop.getSheetName();
        Sheet sheet = wb.createSheet(sheetname);
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).setRandomAccessWindowSize(prop.getStreamRows());
        }
        CreationHelper helper = wb.getCreationHelper();
        int count = 0;
        if (!CollectionUtils.isEmpty(header.getHeaderColumnList()) || !CollectionUtils.isEmpty(header.getHeaderList())) {
            count = generateHeader(sheet, wb, prop, header, helper);
        } else if (!prop.getColumnPropList().isEmpty()) {
            if(ObjectUtils.isEmpty(prop.getTemplateFile())) {
                generateHeader(sheet, wb, prop, header);
            }
            count = prop.getColumnPropList().size();
        }
        fillColumns(wb, sheet, prop, header, helper);
        autoSizeSheet(prop, sheet, count);
        return wb;
    }

    public static void generateExcelFileToLocal(ExcelSheetProp prop, TableConfigProp header, String localPath) throws Exception {
        Workbook wb = generateExcelFile(prop, header);
        try(FileOutputStream out = new FileOutputStream(localPath)) {
            wb.write(out);
            out.close();
            if (wb instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) wb).dispose();
            }
        }catch (IOException ex){
            log.error("{}",ex);
        }
    }

    public static Workbook generateExcelFile(ExcelSheetProp prop, TableConfigProp header, Connection conn, String querySql, Object[] queryParam, ExcelRsExtractor extractor) {
        Object[] objects = generateHeaderWithProp(prop, header);
        Workbook wb = (Workbook) objects[0];
        Sheet sheet = (Sheet) objects[1];
        CreationHelper helper = (CreationHelper) objects[2];
        int count = (Integer) objects[3];
        extractor.setWorkbook(wb);
        extractor.setTargetSheet(sheet);
        extractor.setHelper(helper);

        try {
            SimpleJdbcDao.executeOperationWithQuery(conn, querySql, queryParam, false, extractor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        autoSizeSheet(prop, sheet, count + 1);
        return wb;
    }

    public static Workbook generateExcelFile(ExcelSheetProp prop, TableConfigProp header, Iterator<Map<String, Object>> iterator) {
        Assert.notNull(iterator, "iterator is null");
        Object[] objects = generateHeaderWithProp(prop, header);
        Workbook wb = (Workbook) objects[0];
        Sheet sheet = (Sheet) objects[1];
        CreationHelper helper = (CreationHelper) objects[2];
        int column = (Integer) objects[3];
        int row = 0;
        Map<String, CellStyle> cellMap = new HashMap<>();
        try {
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                processSingleLine(map, wb, sheet, row, prop, header, helper, cellMap);
                if (prop.isStreamInsert() && (row + 1) % prop.getStreamRows() == 0) {
                    ((SXSSFSheet) sheet).flushRows(prop.getStreamRows());
                }
                row++;
            }
        } catch (Exception ex) {

        }
        autoSizeSheet(prop, sheet, column + 1);
        return wb;
    }


    private static Object[] generateHeaderWithProp(ExcelSheetProp prop, TableConfigProp header) {
        Workbook wb = ExcelBaseOper.createWorkBook(prop);
        String sheetname = prop.getSheetName();
        Sheet sheet = wb.createSheet(sheetname);
        CreationHelper helper = wb.getCreationHelper();
        int count = 0;
        if (!header.getHeaderList().isEmpty()) {
            generateHeader(sheet, wb, prop, header);
            count = prop.getColumnList().size();
        } else {
            if(ObjectUtils.isEmpty(prop.getTemplateFile())) {
                generateHeader(sheet, wb, prop, header);
            }
        }
        return new Object[]{wb, sheet, helper, count};
    }

    private static void generateExcelFile(Workbook wb, ExcelSheetProp prop, TableConfigProp header) throws Exception {
        String sheetname = prop.getSheetName();
        Sheet sheet = wb.createSheet(sheetname);
        CreationHelper helper = wb.getCreationHelper();
        int count = 0;
        if (header == null) {
            generateHeader(sheet, wb, prop, header);
            count = prop.getColumnList().size();
        } else {
            count = generateHeader(sheet, wb, prop, header, helper);
        }
        fillColumns(wb, sheet, prop, header, helper);
        autoSizeSheet(prop, sheet, count);
    }

    /**
     * Generate with Mutil Sheet
     *
     * @param propList
     * @return
     */
    public static Workbook generateExcelFileWithMutilSheet(List<ExcelProperty> propList) throws Exception {
        Workbook wb = ExcelBaseOper.createWorkBook(propList.get(0).getSheetProp());

        for (ExcelProperty prop : propList) {
            generateExcelFile(wb, prop.getSheetProp(), prop.getTableProp());
        }
        return wb;
    }


    private static void generateHeader(Sheet targetsheet, Workbook wb, ExcelSheetProp prop) {
        Row row = targetsheet.createRow(0);
        if (!Objects.isNull(prop.getHeaderName())) {
            for (int i = 0; i < prop.getHeaderName().length; i++) {
                String values = prop.getHeaderName()[i];
                if (values != null && !"".equals(values)) {
                    Cell cel = row.createCell(i);

                    CellStyle cellStyle = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, null);
                    cellStyle.setAlignment(HorizontalAlignment.CENTER);
                    cel.setCellValue(values);
                    cel.setCellStyle(cellStyle);
                }
            }
        } else {
            for (int i = 0; i < prop.getColumnPropList().size(); i++) {
                ExcelColumnProp columnProp = prop.getColumnPropList().get(i);
                if (!StringUtils.isEmpty(columnProp.getColumnName())) {
                    Cell cel = row.createCell(i);

                    CellStyle cellStyle = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, null);
                    cellStyle.setAlignment(HorizontalAlignment.CENTER);
                    cel.setCellValue(columnProp.getColumnName());
                    cel.setCellStyle(cellStyle);
                }
            }
        }
    }


    private static void generateHeader(Sheet targetsheet, Workbook wb, ExcelSheetProp prop, TableConfigProp header) {
        Row row = targetsheet.createRow(prop.getStartRow() - 1);
        for (int i = 0; i < prop.getColumnPropList().size(); i++) {
            String values = prop.getColumnPropList().get(i).getColumnName();
            if (values != null && !"".equals(values)) {
                Cell cel = row.createCell(i);

                CellStyle cellStyle = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, header);
                cel.setCellStyle(cellStyle);
                cel.setCellValue(values);

            }
        }
    }

    private static int generateHeader(Sheet targetsheet, Workbook wb, ExcelSheetProp prop, TableConfigProp header, CreationHelper helper) {
        int startcol = prop.getStartCol() - 1;
        int startrow = prop.getStartRow() - 1;

        int contianrows = header.getContainrow();
        Row[] headerRow = new Row[contianrows];
        int tmpcount = startcol;
        for (int pos = 0; pos < contianrows; pos++) {
            headerRow[pos] = ExcelBaseOper.creatRow(targetsheet, startrow
                    + pos);
        }
        if (!CollectionUtils.isEmpty(header.getHeaderColumnList())) {
            int[][] colArr = MergeCellUtil.caculateHeaderRowStartcol(header);
            tmpcount = header.getHeaderColumnList().size();
            for (int i = 0; i < header.getHeaderColumnList().size(); i++) {
                List<TableHeaderColumn> list = header.getHeaderColumnList().get(i);
                for (int j = 0; j < list.size(); j++) {
                    TableHeaderColumn column = list.get(j);
                    column.setStartcol(colArr[i][j]);
                    createHeaderCellRegion(targetsheet, wb, header, header.getHeaderColumnList().get(i).get(j), headerRow, helper);
                }
            }
        } else if (!CollectionUtils.isEmpty(header.getHeaderList())) {
            for (int i = 0; i < header.getHeaderList().size(); i++) {
                TableMergeRegion region = header.getHeaderList().get(i);
                String name = region.getName();
                createHeaderCellRegion(targetsheet, wb, prop, header, region,
                        headerRow, helper, 0, tmpcount, name);
                tmpcount += region.getCollength();
            }
        } else if (!prop.getColumnPropList().isEmpty()) {
            generateHeader(targetsheet, wb, prop, header);
            tmpcount = prop.getStartRow();
        }
        for (int i = 0; i < header.getHeaderList().size(); i++) {
            TableMergeRegion region = header.getHeaderList().get(i);
            createHeaderCellRegion(targetsheet, wb, header, region, headerRow, helper);
        }
        return tmpcount;

    }


    private static void createHeaderCellRegion(Sheet targetsheet, Workbook wb, ExcelSheetProp prop, TableConfigProp header, TableMergeRegion region, Row[] headerRows, CreationHelper helper, int level, int startcol, String value) {

        String name = region.getName();
        Row baserow = headerRows[level];
        int baseRow = baserow.getRowNum();
        if (name != null && !"".equals(name)) {
            if (!CollectionUtils.isEmpty(region.getSubRegions())) {
                if (region.getColheigth() == 1 && region.getCollength() == 1) {
                    CellStyle style = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, region, header);
                    Cell cell = ExcelBaseOper.createCell(baserow, startcol, value, Const.META_TYPE_STRING, style, helper);
                    cell.setCellValue(name);
                } else {
                    CellStyle style = ExcelBaseOper.getHeaderStyle(wb, 3, HorizontalAlignment.CENTER, region, header);
                    ExcelBaseOper.merged(targetsheet, Const.META_TYPE_STRING, baseRow, startcol, baseRow + region.getColheigth() - 1, startcol + region.getCollength() - 1, style, value, helper);
                }
            } else {
                CellStyle style = ExcelBaseOper.getHeaderStyle(wb, 3, HorizontalAlignment.CENTER, region, header);
                ExcelBaseOper.merged(targetsheet, Const.META_TYPE_STRING, baseRow, startcol, baseRow + region.getColheigth() - 1, startcol + region.getCollength() - 1, style, value, helper);
            }
        }
        int tmpcol = startcol;
        if (!ObjectUtils.isEmpty(region.getSubRegions())) {
            for (int j = 0; j < region.getSubRegions().size(); j++) {
                TableMergeRegion newregion = region.getSubRegions().get(j);
                createHeaderCellRegion(targetsheet, wb, prop, header, newregion, headerRows, helper, level + 1, tmpcol, newregion.getName());
                tmpcol += newregion.getCollength();
            }
        }
    }


    private static void createHeaderCellRegion(Sheet targetsheet, Workbook wb, TableConfigProp prop, TableHeaderColumn column, Row[] headerRows, CreationHelper helper) {
        CellStyle style;
        Row baseRow = headerRows[column.getStartrow()];
        if (column.getRowspan() > 1 || column.getColspan() > 1) {
            style = ExcelBaseOper.getHeaderStyle(wb, 3, HorizontalAlignment.CENTER, prop);
            ExcelBaseOper.merged(targetsheet, Const.META_TYPE_STRING, column.getStartrow(), column.getStartcol(), column.getStartrow() + column.getRowspan() - 1, column.getStartcol() + column.getColspan() - 1, style, column.getColumnName(), helper);
        } else {
            style = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, prop);
            ExcelBaseOper.createCell(baseRow, column.getStartcol(), column.getColumnName(), Const.META_TYPE_STRING, style, helper);
        }
    }

    private static void createHeaderCellRegion(Sheet targetsheet, Workbook wb, TableConfigProp prop, TableMergeRegion region, Row[] headerRows, CreationHelper helper) {
        CellStyle style = null;
        Row baseRow = headerRows[region.getStartrow()];
        if (region.getColheigth() > 1 || region.getCollength() > 1) {
            style = ExcelBaseOper.getHeaderStyle(wb, 3, HorizontalAlignment.CENTER, prop);
            ExcelBaseOper.merged(targetsheet, Const.META_TYPE_STRING, region.getStartrow(), region.getStartcol(), region.getStartrow() + region.getColheigth() - 1, region.getStartcol() + region.getCollength() - 1, style, region.getName(), helper);
        } else {
            style = ExcelBaseOper.getHeaderStyle(wb, 1, HorizontalAlignment.CENTER, prop);
            ExcelBaseOper.createCell(baseRow, region.getStartcol(), region.getName(), Const.META_TYPE_STRING, style, helper);
        }
    }

    private static void fillColumns(Workbook wb, Sheet targetsheet, ExcelSheetProp prop, TableConfigProp header, CreationHelper helper) throws IOException {
        try {
            if (!ObjectUtils.isEmpty(prop.getColumnPropList())) {
                List<Map<String, Object>> list = prop.getColumnList();
                //cell style Map
                Map<String, CellStyle> cellMap = new HashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    processSingleLine(list.get(i), wb, targetsheet, i, prop, header, helper, cellMap);
                    if (prop.isStreamInsert() && (i + 1) % prop.getStreamRows() == 0) {
                        ((SXSSFSheet) targetsheet).flushRows(prop.getStreamRows());
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw e;
        }
    }

    public static void processSingleLine(Map<String, ?> map, Workbook wb, Sheet targetsheet, int i, ExcelSheetProp prop, TableConfigProp header, CreationHelper helper, Map<String, CellStyle> cellMap) {
        int startRow = header != null && 0 != header.getContainrow() ? header.getContainrow() : prop.getStartRow() - 1;
        int startCol = prop.getStartCol() - 1;
        int fieldCount = prop.getColumnPropList().size();
        List<Map<String, Object>> list = prop.getColumnList();
        String[] valueArr = new String[fieldCount];
        int[] fromPos = new int[fieldCount];
        boolean[] shallMergin = new boolean[fieldCount];

        Arrays.fill(fromPos, -1);
        Row row = ExcelBaseOper.creatRow(targetsheet, startRow + i);
        for (int j = 0; j < prop.getColumnPropList().size(); j++) {
            ExcelColumnProp excelprop = prop.getColumnPropList().get(j);
            String columnCode = excelprop.getColumnCode();
            String columnType = excelprop.getColumnType();
            boolean needMerge = excelprop.isNeedMerge();
            Object valObj = map.get(columnCode);
            String valueobj;
            if (!StringUtils.isEmpty(map.get(columnCode.toUpperCase()))) {
                valObj = map.get(columnCode.toUpperCase());
            }
            if (valObj == null && !StringUtils.isEmpty(map.get(columnCode.toLowerCase()))) {
                valObj = map.get(columnCode.toLowerCase());
            }
            if (!ObjectUtils.isEmpty(valObj)) {
                valueobj = valObj.toString();
            } else {
                valueobj = "";
            }
            CellStyle stylesingle = ExcelCellStyleUtil.getCellStyle(wb, 1, 1, columnType, header, cellMap);
            CellStyle stylemutil = ExcelCellStyleUtil.getCellStyle(wb, 1, 2, columnType, header, cellMap);
            if (needMerge) {
                if (isFollowingSame(list, i, columnCode)) {
                    valueArr[j] = valueobj;
                    if (fromPos[j] == -1) {
                        fromPos[j] = i;
                    }
                } else if (valueArr[j] != null && !"".equalsIgnoreCase(valueArr[j].trim())) {
                    if (fromPos[j] != -1) {
                        ExcelBaseOper.merged(targetsheet, columnType, fromPos[j] + startRow, startCol + j, i + startRow, startCol + j, stylemutil, valueobj, helper);
                        fromPos[j] = -1;
                        valueArr[j] = "";
                        shallMergin[j] = true;
                    } else {
                        ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
                    }
                } else {
                    ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
                }
            } else {
                if(Const.META_TYPE_FORMULA.equals(excelprop.getColumnType())){
                    valueobj=ExcelBaseOper.returnFormulaWithPos(excelprop.getFormula(),i+prop.getStartRow()+1);
                }
                ExcelBaseOper.createCell(row, j, valueobj, columnType, stylesingle, helper);
            }

        }
        for (int k = 0; k < fieldCount; k++) {
            ExcelColumnProp excelprop = prop.getColumnPropList().get(k);
            boolean needMerge = excelprop.isNeedMerge();
            String columnType = excelprop.getColumnType();
            CellStyle stylesingle = ExcelCellStyleUtil.getCellStyle(wb, 1, 1, columnType, header, cellMap);
            CellStyle stylemutil = ExcelCellStyleUtil.getCellStyle(wb, 1, 2, columnType, header, cellMap);
            if (needMerge) {
                boolean shallParentMerge = shallMergin[k];
                if (shallParentMerge) {
                    for (int p = k; p < fieldCount; p++) {
                        boolean subMerge = prop.getColumnPropList().get(p).isNeedMerge();
                        if (subMerge && valueArr[p] != null && !"".equals(valueArr[p].trim()) && fromPos[p] != -1) {
                            if (fromPos[p] != i) {
                                ExcelBaseOper.merged(targetsheet, columnType, fromPos[p] + startRow, startCol + k, i + startRow, startCol + k, stylemutil, valueArr[p], helper);
                            } else {
                                ExcelBaseOper.createCell(ExcelBaseOper.getRow(targetsheet, fromPos[p] + startRow), p, valueArr[p], columnType, stylesingle, helper);
                            }
                            fromPos[p] = -1;
                            valueArr[p] = "";
                        }
                    }
                    shallMergin[k] = false;
                    break;
                }

            }
        }
        if (prop.getColumnList() != null && i == prop.getColumnList().size() - 1) {
            for (int d = 0; d < prop.getColumnPropList().size(); d++) {
                boolean needMerge = prop.getColumnPropList().get(d).isNeedMerge();
                if (needMerge) {
                    if (fromPos[d] != -1 && !"".equals(valueArr[d])) {
                        CellStyle stylemutil = ExcelCellStyleUtil.getCellStyle(wb, 1, 2, prop.getColumnPropList().get(d).getColumnType(), header, cellMap);
                        ExcelBaseOper.merged(targetsheet, prop.getColumnPropList().get(d).getColumnType(), fromPos[d] + startRow, startCol + i, i + startRow, startCol + i, stylemutil, valueArr[d], helper);
                    } else {
                        String columnCode = prop.getColumnPropList().get(d).getColumnCode();
                        String columnType = prop.getColumnPropList().get(d).getColumnType();
                        CellStyle stylesingle = ExcelCellStyleUtil.getCellStyle(wb, 1, 1, columnType, header, cellMap);
                        Object valObj = map.get(columnCode);
                        String valueobj;

                        if (valObj == null) {
                            valObj = map.get(columnCode.toUpperCase());
                        }
                        if (valObj == null) {
                            valObj = map.get(columnCode.toLowerCase());
                        }
                        if (valObj == null) {
                            valueobj = "";
                        } else {
                            valueobj = valObj.toString();
                        }
                        ExcelBaseOper.createCell(ExcelBaseOper.getRow(targetsheet, startRow + i), d, valueobj, columnType, stylesingle, helper);
                    }
                }
            }
        }
    }

    private static void autoSizeSheet(ExcelSheetProp prop, Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            if (!prop.isStreamInsert()) {
                sheet.autoSizeColumn(i);
            } else {
                if (i == 0) {
                    ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
                }
                sheet.autoSizeColumn(i);
            }
        }

    }

    private static boolean isFollowingSame(List<Map<String, Object>> resultList, int nowpos, String columnName) {
        if (nowpos + 1 < resultList.size()) {
            Object value1 = resultList.get(nowpos).get(columnName);
            Object cmpvalue = resultList.get(nowpos + 1).get(columnName);
            return cmpvalue.equals(value1);
        }
        return false;
    }

    private static void fillColumns(Workbook wb, Sheet targetsheet, ExcelSheetProp prop) {
        if (!CollectionUtils.isEmpty(prop.getColumnList())) {
            int i = prop.getStartRow();
            Iterator<Map<String, Object>> it = prop.getColumnList().iterator();
            Map<String, CellStyle> cellMap = new HashMap<>();
            while (it.hasNext()) {
                Map<String, Object> map = it.next();
                Row row1 = targetsheet.createRow(i - 1);

                for (int j = 0; j < prop.getColumnPropList().size(); j++) {
                    ExcelColumnProp columnProp=prop.getColumnPropList().get(j);
                    String columname = columnProp.getColumnCode();

                    Object valueobj = map.get(columname);
                    if (valueobj == null) {
                        valueobj = map.get(columname.toUpperCase());
                    }
                    if (valueobj == null) {
                        valueobj = map.get(columname.toLowerCase());
                    }
                    String value = "";
                    if (valueobj != null) {
                        value = valueobj.toString();
                    }
                    String colType = columnProp.getColumnType();

                    if (columname != null && !"".equals(columname)) {
                        CellStyle cellStyle = ExcelCellStyleUtil.getCellStyle(wb, 1, 1, colType, null, cellMap);
                        createCell(colType, cellStyle, row1, j, value);
                    }
                }
                i++;
            }

        }

    }

    public static void createCell(String colType, CellStyle cellStyle, Row row1, int j, String value) {
        if (colType.equals(Const.META_TYPE_STRING)) {
            createCell(cellStyle, row1, (short) j, value);
        } else if (colType.equals(Const.META_TYPE_NUMERIC) || colType.equals(Const.META_TYPE_DOUBLE)) {
            if (!StringUtils.isEmpty(value)) {
                createCell(cellStyle, row1, (short) j, Double.parseDouble(value));
            } else {
                createNullCell(cellStyle, row1, (short) j);
            }
        } else if (colType.equals(Const.META_TYPE_BIGINT)) {
            if (!StringUtils.isEmpty(value)) {
                createCell(cellStyle, row1, (short) j, Long.parseLong(value));
            } else {
                createNullCell(cellStyle, row1, (short) j);
            }
        } else if (colType.equalsIgnoreCase(Const.META_TYPE_INTEGER)) {
            if (!StringUtils.isEmpty(value)) {
                createCell(cellStyle, row1, (short) j, Integer.parseInt(value));
            } else {
                createNullCell(cellStyle, row1, (short) j);
            }
        } else if (colType.equals(Const.META_TYPE_DATE)) {
            if (!"".equals(value)) {
                createCellDate(cellStyle, row1, (short) j, value);
            }
        } else {
            createCell(cellStyle, row1, (short) j, value);
        }
    }


    private static void createCell(CellStyle cellStyle, Row row, int column, String objvalue) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
        String value = "";
        if (objvalue != null) {
            value = objvalue;
        }
        cell.setCellValue(value);
    }

    private static void createNullCell(CellStyle cellStyle, Row row, int column) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
    }

    private static void createCell(CellStyle cellStyle, Row row, int column, Object value) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(cellStyle);
        Assert.notNull(value, "");
        if (Double.class.isAssignableFrom(value.getClass())) {
            cell.setCellValue((Double) value);
        } else if (Long.class.isAssignableFrom(value.getClass())) {
            cell.setCellValue((Long) value);
        } else if (Integer.class.isAssignableFrom(value.getClass())) {
            cell.setCellValue((Integer) value);
        }

    }

    private static void createCellDate(CellStyle cellStyle, Row row, int column, String value) {
        Cell cell = row.createCell(column);
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("yyyy-MM-dd hh:mm:ss"));
        cell.setCellStyle(cellStyle);
        cell.setCellValue(value);
    }

    private static int[] getExcelPosition(String startPos, String endPos) {
        int startPosLen = startPos.length();
        int endPosLen = endPos.length();
        int startRow = 0;
        int startCol = 0;
        int endRow = 0;
        int endCol = 0;
        for (int i = 0; i < startPosLen; i++) {
            if (isChar(startPos.charAt(i))) {
                startCol = i * 26 + getDigintalByChar(startPos.toUpperCase().charAt(i));
            } else {
                startRow = Integer.parseInt(startPos.substring(i));
                break;
            }
        }
        for (int j = 0; j < endPosLen; j++) {
            if (isChar(endPos.charAt(j))) {
                endCol = endCol * 26 + getDigintalByChar(endPos.toUpperCase().charAt(j));
            } else {
                endRow = Integer.parseInt(endPos.substring(j));
                break;
            }
        }
        return new int[]{startCol, endCol, startRow, endRow};
    }

    private static boolean isChar(char str) {
        return Pattern.matches("[A-Z]", String.valueOf(str));

    }

    public static boolean isValidExcelInput(String str) {
        return Pattern.matches("[A-Z]+\\d+", str);
    }

    private static int getDigintalByChar(char str) {
        int startChar = 'A';
        return str - startChar + 1;
    }


}
