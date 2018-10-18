package com.robin.comm.util.xls;

import com.robin.core.query.extractor.ResultSetOperationExtractor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.sql.SQLException;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.comm.util.xls</p>
 * <p>
 * <p>Copyright: Copyright (c) 2017 create at 2017年12月19日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ExcelRsExtractor extends ResultSetOperationExtractor {
    public Workbook workbook;
    Sheet targetSheet;
    ExcelSheetProp prop;
    TableHeaderProp header;
    CreationHelper helper;

    int pos = 2;

    public ExcelRsExtractor(ExcelSheetProp prop,TableHeaderProp header) {
        this.prop=prop;
        this.header=header;
        pos=prop.getStartRow();
    }

    @Override
    public boolean executeAddtionalOperation(Map<String, Object> map, String[] columnName, String[] typeName, String[] className) throws SQLException {
        ExcelGenerator.processSingleLine(map, workbook,targetSheet,pos,prop,header,helper);
        pos++;
        return true;
    }

    @Override
    public void init() {

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
