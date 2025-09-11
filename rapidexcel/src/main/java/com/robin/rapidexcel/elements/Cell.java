package com.robin.rapidexcel.elements;

import lombok.Getter;

@Getter
public class Cell {
    private WorkBook workBook;
    private CellType type;
    private Object value;
    private CellAddress address;
    private String formula;
    private String rawValue;
    private String dataFormatId;
    private String dataFormatString;
    public Cell(WorkBook workbook, CellType type, Object value, CellAddress address, String formula, String rawValue) {
        this(workbook, type, value, address, formula, rawValue, null, null);
    }

    public Cell(WorkBook workbook, CellType type, Object value, CellAddress address, String formula, String rawValue,
         String dataFormatId, String dataFormatString) {
        this.workBook = workbook;
        this.type = type;
        this.value = value;
        this.address = address;
        this.formula = formula;
        this.rawValue = rawValue;
        this.dataFormatId = dataFormatId;
        this.dataFormatString = dataFormatString;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
