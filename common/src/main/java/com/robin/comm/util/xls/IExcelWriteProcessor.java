package com.robin.comm.util.xls;


import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

@FunctionalInterface
public interface IExcelWriteProcessor {
    void doRead(Workbook book, FormulaEvaluator evaluator);
}
