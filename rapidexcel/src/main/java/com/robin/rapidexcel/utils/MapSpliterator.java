package com.robin.rapidexcel.utils;

import com.robin.comm.util.xls.ExcelSheetProp;
import com.robin.rapidexcel.elements.Cell;
import com.robin.rapidexcel.elements.CellAddress;
import com.robin.rapidexcel.elements.WorkBook;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSpliterator extends BaseSpliterator<Map<String,Object>> {
    private Map<String,Object> valueMap=new HashMap<>();
    private List<Cell> cells=new ArrayList<>();

    public MapSpliterator(WorkBook workBook, InputStream stream, ExcelSheetProp prop) throws XMLStreamException {
        super(workBook, stream, prop);
        multipleType=false;
    }

    @Override
    void initHeader() {
        cells=new ArrayList<>(prop.getColumnList().size());
        for(int i=0;i<prop.getColumnPropList().size();i++){
            cells.add(null);
        }
    }

    @Override
    Map<String,Object> next() throws XMLStreamException {
        return super.next();
    }

    @Override
    void processCell(int trackedColIndex) throws XMLStreamException {
        Cell cell = parseCell(trackedColIndex,true);
        CellAddress addr = cell.getAddress();
        cells.set(addr.getColumn(), cell);
    }

    @Override
    Map constructReturn() {
        if(!prop.isStreamMode()){
            valueMap=new HashMap<>();
        }
        for(int i=0;i<prop.getColumnPropList().size();i++){
            if(cells.get(i)!=null && cells.get(i).getValue()!=null){
                valueMap.put(prop.getColumnPropList().get(i).getColumnCode(),cells.get(i).getValue());
            }
        }
        return valueMap;
    }



}
