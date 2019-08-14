package com.robin.comm.util.xls;

import com.robin.core.query.extractor.ResultSetOperationExtractor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;


public class ExcelRsExtractor extends ResultSetOperationExtractor {
    public Workbook workbook;
    Sheet targetSheet;
    ExcelSheetProp prop;
    TableHeaderProp header;
    CreationHelper helper;

    int pos;
    int processRows = 0;

    public ExcelRsExtractor(ExcelSheetProp prop, TableHeaderProp header) {
        this.prop = prop;
        this.header = header;
        pos = prop.getStartRow();
        super.init();
    }

    @Override
    public boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd) throws SQLException {
        ExcelProcessor.processSingleLine(map, workbook, targetSheet, pos, prop, header, helper);
        pos++;
        processRows++;
        try {
            if (prop.isBatchInsert() && (processRows) % prop.getBatchRows() == 0) {
                ((SXSSFSheet) targetSheet).flushRows(prop.getBatchRows());
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
