package com.robin.rapidexcel.utils;

import com.robin.comm.util.xls.ExcelColumnProp;
import com.robin.comm.util.xls.ExcelSheetProp;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.rapidexcel.elements.Cell;
import com.robin.rapidexcel.elements.CellAddress;
import com.robin.rapidexcel.elements.CellType;
import com.robin.rapidexcel.elements.WorkBook;
import com.robin.rapidexcel.reader.XMLReader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

abstract class BaseSpliterator<T> implements Spliterator<T> {
    final XMLReader r;
    static WorkBook workBook;
    int trackedRowIndex = 0;
    boolean containHeaders=false;
    static ExcelSheetProp prop;
    boolean multipleType=false;
    boolean needIdentifyColumn=false;
    List<Cell> cells=new ArrayList<>();


    public BaseSpliterator(WorkBook workBook, InputStream stream, ExcelSheetProp prop) throws XMLStreamException {
        this.workBook=workBook;
        this.r =new XMLReader(XMLFactoryUtils.getDefaultInputFactory(),stream);
        this.prop=prop;
        if(prop!=null){
            containHeaders=prop.isFillHeader();
            if(CollectionUtils.isEmpty(prop.getColumnPropList())){
                needIdentifyColumn=true;
            }
        }else{
            containHeaders=true;
            needIdentifyColumn=true;
            prop=new ExcelSheetProp();
        }
        r.goTo("sheetData");
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        try {
            if (hasNext()) {
                action.accept(next());
                return true;
            } else {
                return false;
            }
        } catch (XMLStreamException e) {
            throw new OperationNotSupportException(e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | IMMUTABLE | NONNULL | ORDERED;
    }


    boolean hasNext() throws XMLStreamException {
        if(containHeaders){
            readHeader();
        }
        if (r.goTo(() -> r.isStartElement("row") || r.isEndElement("sheetData"))) {
            return "row".equals(r.getLocalName());
        } else {
            return false;
        }
    }
    void readHeader() throws XMLStreamException{
        int trackedColIndex=0;
        while (r.goTo(() -> r.isStartElement("c") || r.isEndElement("row"))) {
            if ("row".equals(r.getLocalName())) {
                break;
            }
            CellAddress addr = getCellAddressWithFallback(trackedColIndex);
            Cell cell = parseCell(trackedColIndex++,false);
            if(needIdentifyColumn && !ObjectUtils.isEmpty(cell.getValue())){
                prop.addColumnProp(new ExcelColumnProp(cell.getValue().toString(),cell.getValue().toString(), Const.META_TYPE_STRING));
            }
        }
        initHeader();
    }
    abstract void initHeader();
    CellAddress getCellAddressWithFallback(int trackedColIndex) {
        String cellRefOrNull = r.getAttribute("r");
        return cellRefOrNull != null ?
                new CellAddress(cellRefOrNull) :
                new CellAddress(trackedRowIndex, trackedColIndex);
    }

    T next() throws XMLStreamException {
        if (!"row".equals(r.getLocalName())) {
            throw new NoSuchElementException();
        }
        int trackedColIndex = 0;
        while (r.goTo(() -> r.isStartElement("c") || r.isEndElement("row"))) {
            if ("row".equals(r.getLocalName())) {
                break;
            }
            processCell(trackedColIndex++);
        }
        return constructReturn();
    }

    CellType parseType(String type) {
        switch (type) {
            case "b":
                return CellType.BOOLEAN;
            case "e":
                return CellType.ERROR;
            case "n":
                return CellType.NUMBER;
            case "str":
                return CellType.FORMULA;
            case "s":
            case "inlineStr":
                return CellType.STRING;
        }
        throw new IllegalStateException("Unknown cell type : " + type);
    }

    BiFunction<String, CellAddress,?> getParserForType(CellType type) {
        switch (type) {
            case BOOLEAN:
                return BaseSpliterator::parseBoolean;
            case NUMBER:
                return BaseSpliterator::parseNumber;
            case FORMULA:
            case ERROR:
                return BaseSpliterator::defaultValue;
        }
        throw new IllegalStateException("No parser defined for type " + type);
    }



    private static Object parseNumber(String s,CellAddress address) {
        try {
            if(ObjectUtils.isEmpty(s)){
                return null;
            }
            int column=address.getColumn();
            String tmpVal=s;
            Object retObj=null;
            if(!CollectionUtils.isEmpty(prop.getColumnPropList()) && prop.getColumnPropList().get(column)!=null){
                ExcelColumnProp columnProp=prop.getColumnPropList().get(column);
                switch (columnProp.getColumnType()){
                    case Const.META_TYPE_INTEGER:
                        if(tmpVal.contains(".")){
                            int pos=tmpVal.indexOf(".");
                            tmpVal=tmpVal.substring(0,pos);
                        }
                        retObj=Integer.parseInt(tmpVal);
                        break;
                    case Const.META_TYPE_BIGINT:
                        if(tmpVal.contains(".")){
                            int pos=tmpVal.indexOf(".");
                            tmpVal=tmpVal.substring(0,pos);
                        }
                        retObj=Long.parseLong(tmpVal);
                        break;
                    case Const.META_TYPE_FLOAT:
                        retObj=Float.parseFloat(tmpVal);
                        break;
                    case Const.META_TYPE_DOUBLE:
                    case Const.META_TYPE_NUMERIC:
                        retObj=Double.parseDouble(tmpVal);
                        break;
                    case Const.META_TYPE_TIMESTAMP:
                        retObj=DateUtils.getLocalDateTime(Double.valueOf(s),workBook.isDate1904(),false);
                        break;
                    default:
                        retObj=new BigDecimal(s);
                }
            }else{
                retObj=new BigDecimal(s);
            }
            return retObj;
        } catch (NumberFormatException e) {
            throw new OperationNotSupportException("Cannot parse number : " + s);
        }
    }
    private static String defaultValue(String s,CellAddress address){
        return s;
    }

    private static Boolean parseBoolean(String s,CellAddress address) {
        if ("0".equals(s)) {
            return Boolean.FALSE;
        } else if ("1".equals(s)) {
            return Boolean.TRUE;
        } else {
            throw new OperationNotSupportException("Invalid boolean cell value: '" + s + "'. Expecting '0' or '1'.");
        }
    }
    Cell parseCell(int trackedColIndex,boolean isMultiplex) throws XMLStreamException {
        CellAddress addr = getCellAddressWithFallback(trackedColIndex);
        String type = r.getOptionalAttribute("t").orElse("n");
        String styleString = r.getAttribute("s");
        String formatId = null;
        String formatString = null;
        if (styleString != null) {
            int index = Integer.parseInt(styleString);
            if (index < workBook.getFormats().size()) {
                formatId = workBook.getFormats().get(index);
                formatString = workBook.getNumFmtMap().get(formatId);
            }
        }

        if ("inlineStr".equals(type)) {
            return parseInlineStr(addr,isMultiplex);
        } else if ("s".equals(type)) {
            return parseString(addr,isMultiplex);
        } else {
            return parseOther(addr, type, formatId, formatString,isMultiplex);
        }
    }

    Cell parseInlineStr(CellAddress addr,boolean isMultiplex) throws XMLStreamException {
        Object value = null;
        String formula = null;
        String rawValue = null;
        while (r.goTo(() -> r.isStartElement("is") || r.isEndElement("c") || r.isStartElement("f"))) {
            if ("is".equals(r.getLocalName())) {
                rawValue = r.getValueUntilEndElement("is");
                value = rawValue;
            } else if ("f".equals(r.getLocalName())) {
                formula = r.getValueUntilEndElement("f");
            } else {
                break;
            }
        }
        CellType cellType = formula == null ? CellType.STRING : CellType.FORMULA;
        return  returnCell(isMultiplex,workBook,cellType,value,addr,formula,rawValue);
    }
    Cell empty(CellAddress addr, CellType type) {
        return new Cell(workBook, type, "", addr, null, "");
    }
    Cell parseString(CellAddress addr,boolean isMultiplex) throws XMLStreamException {
        r.goTo(() -> r.isStartElement("v") || r.isEndElement("c"));
        if (r.isEndElement("c")) {
            return empty(addr, CellType.STRING);
        }
        String v = r.getValueUntilEndElement("v");
        if (v.isEmpty()) {
            return empty(addr, CellType.STRING);
        }
        int index = Integer.parseInt(v);
        String sharedStringValue = workBook.getShardingStrings().getValues().get(index);
        Object value = sharedStringValue;
        String formula = null;
        String rawValue = sharedStringValue;
        return  returnCell(isMultiplex,workBook,CellType.STRING,value,addr,formula,rawValue);
    }
    Cell parseOther(CellAddress addr, String type, String dataFormatId, String dataFormatString,boolean isMultiplex)
            throws XMLStreamException {
        CellType definedType = parseType(type);
        BiFunction<String,CellAddress, ?> parser = getParserForType(definedType);

        Object value = null;
        String formula = null;
        String rawValue = null;
        while (r.goTo(() -> r.isStartElement("v") || r.isEndElement("c") || r.isStartElement("f"))) {
            if ("v".equals(r.getLocalName())) {
                rawValue = r.getValueUntilEndElement("v");
                try {
                    value = "".equals(rawValue) ? null : parser.apply(rawValue,addr);
                } catch (OperationNotSupportException e) {
                    definedType = CellType.ERROR;
                }
            } else if ("f".equals(r.getLocalName())) {
                String ref = r.getAttribute("ref");
                String t = r.getAttribute("t");
                String si = r.getAttribute("si");
                Integer siInt = si == null ? null : Integer.parseInt(si);
                formula = r.getValueUntilEndElement("f");

            } else {
                break;
            }
        }
        if (formula == null && value == null && definedType == CellType.NUMBER) {
            //return new Cell(workBook, CellType.EMPTY, null, addr, null, rawValue);
            return returnCell(isMultiplex,workBook, CellType.EMPTY, null, addr, null, rawValue);
        } else {
            CellType cellType = (formula != null) ? CellType.FORMULA : definedType;
            //return new Cell(workBook, cellType, value, addr, formula, rawValue, dataFormatId, dataFormatString);
            return returnCell(isMultiplex,workBook, cellType, value, addr, formula, rawValue, dataFormatId, dataFormatString);
        }
    }
    abstract T constructReturn();

    Cell returnCell(boolean isMultiplex,WorkBook workBook, CellType type, Object value, CellAddress addr, String formula, String rawValue) {
        if(!isMultiplex || valueAllEmpty() || cells.get(addr.getColumn())==null) {
            return new Cell(workBook, type, value, addr, formula, rawValue);
        }else{
            Cell cell=cells.get(addr.getColumn());
            if(value!=null){
                cell.setValue(value);
            }
            return cell;
        }
    }

    Cell returnCell(boolean isMultiplex,WorkBook workBook, CellType type, Object value, CellAddress addr, String formula, String rawValue, String dataFormatId, String dataFormatString) {
        if(!isMultiplex || valueAllEmpty() || cells.get(addr.getColumn())==null) {
            return new Cell(workBook, type, value, addr, formula, rawValue,dataFormatId,dataFormatString);
        }else{
            Cell cell=cells.get(addr.getColumn());
            if(value!=null){
                cell.setValue(value);
            }
            return cell;
        }
    }
    boolean valueAllEmpty(){
        return CollectionUtils.isEmpty(cells) || cells.stream().allMatch(ObjectUtils::isEmpty);
    }
    abstract void processCell(int trackedColIndex) throws XMLStreamException;
}
