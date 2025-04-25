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

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;

import com.robin.core.base.util.Const;

public class ExcelCellStyleUtil {

    private static final String DEFAULT_FONT_NAME = java.awt.Font.SANS_SERIF;

    private ExcelCellStyleUtil() {

    }

    public static CellStyle getNoBorderCellType(Workbook wb, String metaType) {
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setWrapText(true);
        extractMeta(wb,metaType, cs);
        return cs;
    }

    private static void extractMeta(Workbook wb,String metaType, CellStyle cs) {
        switch (metaType) {
            case Const.META_TYPE_NUMERIC:
            case Const.META_TYPE_DOUBLE:
            case Const.META_TYPE_FLOAT:
                cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));
                break;
            case Const.META_TYPE_DATE:
            case Const.META_TYPE_TIMESTAMP:
                cs.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
                break;
            case Const.META_TYPE_INTEGER:
                cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
                break;
        }
    }

    public static CellStyle getBorderCellType(Workbook wb, String metaType) {
        CellStyle cs = wb.createCellStyle();
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderRight(BorderStyle.THIN);
        cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cs.setBorderTop(BorderStyle.THIN);
        cs.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setWrapText(true);
        Font font = wb.createFont();
        font.setFontName(ExcelBaseOper.defaultFontName);
        cs.setFont(font);
        extractMeta(wb,metaType, cs);
        return cs;

    }
    public static CellStyle getCellStyle(Workbook wb,String metaType,Map<String, CellStyle> cellMap){
        if(cellMap.containsKey(metaType)){
            return cellMap.get(metaType);
        }else{
            CellStyle cs = wb.createCellStyle();
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            cs.setAlignment(HorizontalAlignment.CENTER);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderRight(BorderStyle.THIN);
            cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderTop(BorderStyle.THIN);
            cs.setTopBorderColor(IndexedColors.BLACK.getIndex());
            cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            Font font = wb.createFont();
            font.setFontName(ExcelBaseOper.defaultFontName);
            cs.setFont(font);
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setWrapText(true);
            extractMeta(wb,metaType,cs);
            cellMap.put(metaType,cs);
            return cs;
        }
    }

    public static CellStyle getCellStyle(Workbook wb, int rowspan, int colspan, String metaType, TableConfigProp header, Map<String, CellStyle> cellMap) {
        CellStyle cs = null;
        if (cellMap.containsKey("C_" + rowspan + "_" + colspan + "_" + metaType)) {
            cs = cellMap.get("C_" + rowspan + "_" + colspan + "_" + metaType);
        } else {
            cs = wb.createCellStyle();
            if (rowspan > 1) {
                cs.setVerticalAlignment(VerticalAlignment.CENTER);
            }
            cs.setAlignment(HorizontalAlignment.CENTER);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderRight(BorderStyle.THIN);
            cs.setRightBorderColor(IndexedColors.BLACK.getIndex());
            cs.setBorderTop(BorderStyle.THIN);
            cs.setTopBorderColor(IndexedColors.BLACK.getIndex());
            cs.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            Font font = wb.createFont();
            font.setFontName(ExcelBaseOper.defaultFontName);
            if (header != null) {
                font.setFontName((header.getContentFontName() == null || header.getContentFontName().isEmpty()) ? DEFAULT_FONT_NAME : header.getContentFontName());
                if (header.isBold()) {
                    font.setBold(true);
                }
                if (header.isItalic()) {
                    font.setItalic(true);
                }
            }
            cs.setFont(font);
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setWrapText(true);
            extractMeta(wb,metaType,cs);
            cellMap.put("C_" + rowspan + "_" + colspan + "_" + metaType, cs);
        }
        return cs;
    }


}
