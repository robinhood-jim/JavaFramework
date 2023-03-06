package com.robin.comm.util.xls;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IExcelReadProcessor {
    @Nullable
    void doRead(Workbook book, Row row, FormulaEvaluator evaluator);
}
