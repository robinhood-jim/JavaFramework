package com.robin.rapidexcel.elements;

import java.util.List;

public class WorkSheet {
    private String id;
    private int index;
    private String name;
    private String sheetId;
    private SheetVisibility visibility;
    private WorkBook workBook;

    private List<Row> rows;
    public WorkSheet(WorkBook workBook,int index,String id,String sheetId,String name,SheetVisibility visibility){
        this.index=index;
        this.id=id;
        this.name=name;
        this.visibility=visibility;
        this.sheetId=sheetId;
        this.workBook=workBook;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
