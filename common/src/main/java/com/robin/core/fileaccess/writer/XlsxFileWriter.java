package com.robin.core.fileaccess.writer;

import com.robin.comm.util.xls.ExcelBaseOper;
import com.robin.comm.util.xls.ExcelCellStyleUtil;
import com.robin.comm.util.xls.ExcelColumnProp;
import com.robin.comm.util.xls.ExcelSheetProp;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ObjectUtils;
import org.tukaani.xz.FinishableOutputStream;

import javax.naming.OperationNotSupportedException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XlsxFileWriter extends TextBasedFileWriter{
    private XMLOutputFactory factory;
    private XMLEventFactory ef = XMLEventFactory.newInstance();
    private XMLStreamWriter streamWriter;
    private XSSFWorkbook workbook;
    private OPCPackage opcPackage;
    private PackagePart part;
    private OutputStream xlsxOutputStream;
    private static final char CHARA = 'A';
    private Map<String,CellStyle> cellStyleMap=new HashMap<>();
    private int rowPos=2;
    private ExcelSheetProp sheetProp;
    public XlsxFileWriter(){
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }
    public XlsxFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }
    public XlsxFileWriter(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta,accessor);
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }

    @Override
    public void beginWrite() throws IOException {
        super.beginWrite();
        try {
            factory=XMLOutputFactory.newFactory();
            workbook = new XSSFWorkbook();
            Field field = workbook.getClass().getSuperclass().getDeclaredField("pkg");
            field.setAccessible(true);
            opcPackage = (OPCPackage) field.get(workbook);
            workbook.createSheet("sheet1");
            PackagePartName packagePartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet1.xml");
            opcPackage.removePart(packagePartName);
            part = opcPackage.createPart(packagePartName, XSSFRelation.WORKSHEET.getContentType());
            opcPackage.addRelationship(packagePartName, TargetMode.INTERNAL,XSSFRelation.WORKSHEET.getRelation());
            opcPackage.flush();
            xlsxOutputStream=part.getOutputStream();
            streamWriter = factory.createXMLStreamWriter(xlsxOutputStream,colmeta.getEncode());
            streamWriter.writeStartDocument("UTF-8", "1.0");
            streamWriter.writeStartElement("worksheet");
            streamWriter.writeAttribute("xmlxs", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
            streamWriter.writeEmptyElement("dimension");
            streamWriter.writeAttribute("ref", "A1");
            streamWriter.writeStartElement("sheetViews");
            streamWriter.writeStartElement("sheetView");
            streamWriter.writeAttribute("workbookViewId", "0");
            streamWriter.writeAttribute("tabSelected", "true");
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEmptyElement("sheetFormatPr");
            streamWriter.writeAttribute("defaultRowHeight", "15.0");
            streamWriter.writeStartElement("cols");
            streamWriter.writeStartElement("col");
            streamWriter.writeAttribute("min", "1");
            streamWriter.writeAttribute("max", "1");
            streamWriter.writeAttribute("customWidth", "true");
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeStartElement("sheetData");
            writeHeader(sheetProp.getColumnPropList().stream().map(ExcelColumnProp::getColumnName).collect(Collectors.toList()),cellStyleMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void finishWrite() throws IOException {
        try{
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            if(!(out instanceof FinishableOutputStream)) {
                streamWriter.flush();
                writer.flush();
            }else{
                ((FinishableOutputStream) out).finish();
            }

        }catch (Exception ex){
            throw new IOException(ex);
        }finally {
            try {
                streamWriter.close();
            }catch (XMLStreamException ex){
                ex.printStackTrace();
            }
            part.flush();
            xlsxOutputStream.flush();
            xlsxOutputStream.close();
            part.close();

            if(workbook!=null){
                Sheet sheet=workbook.getSheetAt(0);
                for(int i=0;i<colmeta.getColumnList().size();i++){
                    sheet.autoSizeColumn(i);
                }
                workbook.write(out);
                workbook.close();
            }
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        try{
            writeLine(map,colmeta,cellStyleMap,rowPos++);
        }catch (XMLStreamException ex){
            throw new IOException(ex);
        }
    }
    private String getEndCell(int columnSize) {
        int firstPos = columnSize / 26;
        int secondPos = columnSize % 26;
        StringBuilder builder = new StringBuilder();
        if (firstPos > 0) {
            builder.append((char)(CHARA + firstPos - 1));
        }
        builder.append((char)(CHARA + secondPos - 1));
        return builder.toString();
    }
    private void writeLine(Map<String, Object> valueMap, DataCollectionMeta colmeta, Map<String, CellStyle> cellStyleMap, int rowpos) throws XMLStreamException {

        streamWriter.writeStartElement("row");
        streamWriter.writeAttribute("r", String.valueOf(rowpos));
        for (int i = 0; i < colmeta.getColumnList().size(); i++) {
            CellStyle style = ExcelCellStyleUtil.getCellStyle(workbook, colmeta.getColumnList().get(i).getColumnType(), cellStyleMap);
            if (!ObjectUtils.isEmpty(valueMap.get(colmeta.getColumnList().get(i).getColumnName()))) {
                streamWriter.writeStartElement("c");
                streamWriter.writeAttribute("r", getEndCell(i + 1) + rowpos);
                streamWriter.writeAttribute("s", String.valueOf(style.getFontIndexAsInt()));
                if (Const.META_TYPE_STRING.equals(colmeta.getColumnList().get(i).getColumnType())) {
                    streamWriter.writeAttribute("t", "inlineStr");
                    streamWriter.writeStartElement("is");
                    streamWriter.writeStartElement("t");
                    streamWriter.writeCharacters(valueMap.get(colmeta.getColumnList().get(i).getColumnName()).toString());
                    streamWriter.writeEndElement();
                    streamWriter.writeEndElement();
                } else {
                    streamWriter.writeAttribute("t", "n");

                    if (Const.META_TYPE_TIMESTAMP.equals(colmeta.getColumnList().get(i).getColumnType()) || Const.META_TYPE_DATE.equals(colmeta.getColumnList().get(i).getColumnType())) {
                        streamWriter.writeStartElement("v");
                        Object value = valueMap.get(colmeta.getColumnList().get(i).getColumnName());
                        Date valDate = null;
                        double dateVal=0.0;
                        if (Timestamp.class.isAssignableFrom(value.getClass())) {
                            valDate = new Date(((Timestamp) value).getTime());
                            dateVal = DateUtil.getExcelDate(valDate);
                        } else if(LocalDateTime.class.isAssignableFrom(value.getClass())){
                            dateVal=DateUtil.getExcelDate((LocalDateTime) value);
                        }else if(LocalDate.class.isAssignableFrom(value.getClass())){
                            dateVal=DateUtil.getExcelDate((LocalDate) value);
                        }
                        else if(Date.class.isAssignableFrom(value.getClass())){
                            dateVal=DateUtil.getExcelDate((Date) value);
                        }else{
                            dateVal=DateUtil.getExcelDate(new Date(Long.valueOf(value.toString())));
                        }
                        streamWriter.writeCharacters(String.valueOf(dateVal));
                    }
                    else {
                        streamWriter.writeStartElement("v");
                        streamWriter.writeCharacters(valueMap.get(colmeta.getColumnList().get(i).getColumnName()).toString());
                    }
                    streamWriter.writeEndElement();
                }
                streamWriter.writeEndElement();
            }else if(Const.META_TYPE_FORMULA.equals(sheetProp.getColumnPropList().get(i).getColumnType())){
                streamWriter.writeStartElement("c");
                streamWriter.writeAttribute("r", getEndCell(i + 1) + rowpos);
                streamWriter.writeAttribute("s", String.valueOf(style.getFontIndexAsInt()));
                streamWriter.writeStartElement("f");
                streamWriter.writeCharacters(ExcelBaseOper.returnFormulaWithPos(sheetProp.getColumnPropList().get(i).getFormula(),rowpos));
                streamWriter.writeEndElement();
                streamWriter.writeEndElement();
            }
        }
        streamWriter.writeEndElement();
        streamWriter.writeCharacters("\n");
    }

    public void setSheetProp(ExcelSheetProp sheetProp) {
        this.sheetProp = sheetProp;
    }

    private void writeHeader(List<String> columnName, Map<String, CellStyle> cellStyleMap) throws Exception {
        CellStyle style = ExcelCellStyleUtil.getCellStyle(workbook, Const.META_TYPE_STRING, cellStyleMap);
        streamWriter.writeStartElement("row");
        streamWriter.writeAttribute("r", "1");
        for (int i = 0; i < columnName.size(); i++) {
            streamWriter.writeStartElement("c");
            streamWriter.writeAttribute("r", getEndCell(i + 1) + "1");
            streamWriter.writeAttribute("s", String.valueOf(style.getIndex()));
            streamWriter.writeAttribute("t", "inlineStr");
            streamWriter.writeStartElement("is");
            streamWriter.writeStartElement("t");
            streamWriter.writeCharacters(columnName.get(i));
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
        }
        streamWriter.writeEndElement();
    }
}
