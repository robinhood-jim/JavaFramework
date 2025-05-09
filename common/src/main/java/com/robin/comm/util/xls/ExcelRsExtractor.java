package com.robin.comm.util.xls;

import com.robin.core.query.extractor.ResultSetOperationExtractor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ExcelRsExtractor extends ResultSetOperationExtractor {
    public Workbook workbook;
    Sheet targetSheet;
    ExcelSheetProp prop;
    TableConfigProp header;
    CreationHelper helper;
    //cell style map
    Map<String, CellStyle> cellMap = new HashMap<>();

    int pos;
    int processRows = 0;

    public ExcelRsExtractor(ExcelSheetProp prop, TableConfigProp header) {
        this.prop = prop;
        this.header = header;
        pos = prop.getStartRow();
        super.init();
    }

    @Override
    public boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd) throws SQLException {
        ExcelProcessor.processSingleLine(map, workbook, targetSheet, pos, prop, header, helper,cellMap);
        pos++;
        processRows++;
        try {
            if (prop.isStreamMode() && (processRows) % prop.getStreamRows() == 0) {
                ((SXSSFSheet) targetSheet).flushRows(prop.getStreamRows());
            }

        }catch (Exception ex){
            throw new SQLException(ex);
        }
        return true;
    }


    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public void setTargetSheet(Sheet targetSheet) {
        this.targetSheet = targetSheet;
    }

    public void setHelper(CreationHelper helper) {
        this.helper = helper;
    }

}
