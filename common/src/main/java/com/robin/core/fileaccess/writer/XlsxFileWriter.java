package com.robin.core.fileaccess.writer;

import cn.hutool.core.io.FileUtil;
import com.robin.comm.util.xls.ExcelBaseOper;
import com.robin.comm.util.xls.ExcelCellStyleUtil;
import com.robin.comm.util.xls.ExcelColumnProp;
import com.robin.comm.util.xls.ExcelSheetProp;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ObjectUtils;
import org.tukaani.xz.FinishableOutputStream;

import javax.naming.OperationNotSupportedException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Xlsx StaX writer,Support divided Sheet write,MAX_LINE max sheet lines
 */
public class XlsxFileWriter extends TextBasedFileWriter{
    private XMLStreamWriter streamWriter;
    private XSSFWorkbook workbook;
    private OPCPackage opcPackage;

    private static final char CHARA = 'A';
    private final Map<String,CellStyle> cellStyleMap=new HashMap<>();
    private int rowPos=2;
    private int currentSheetPos=0;
    private ExcelSheetProp sheetProp;
    private ZipOutputStream zipOutputStream;
    private BufferedOutputStream bufferedOutputStream;
    private boolean multipleSheets=false;
    private ByteArrayOutputStream byteOut;
    private FileOutputStream tmpZipFile;
    private String tmpFileName;
    private int MAX_LINE=Double.valueOf(Math.pow(2,18)).intValue();
    public XlsxFileWriter(){
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }
    public XlsxFileWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
        if(colmeta.getResourceCfgMap().containsKey(Const.COLUMN_XLSX_MULTIPLESHEETS) && Const.TRUE.equalsIgnoreCase(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_MULTIPLESHEETS).toString())){
            multipleSheets=true;
        }
        if(colmeta.getResourceCfgMap().containsKey(Const.COLUMN_XLSX_SHEETLINELIMITS) && NumberUtils.isNumber(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_SHEETLINELIMITS).toString())){
            MAX_LINE=Integer.parseInt(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_SHEETLINELIMITS).toString());
        }
    }
    public XlsxFileWriter(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
        super(colmeta,accessor);
        this.identifier= Const.FILEFORMATSTR.XLSX.getValue();
        if(colmeta.getResourceCfgMap().containsKey(Const.COLUMN_XLSX_MULTIPLESHEETS) && Const.TRUE.equalsIgnoreCase(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_MULTIPLESHEETS).toString())){
            multipleSheets=true;
        }
        if(colmeta.getResourceCfgMap().containsKey(Const.COLUMN_XLSX_SHEETLINELIMITS) && NumberUtils.isNumber(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_SHEETLINELIMITS).toString())){
            MAX_LINE=Integer.parseInt(colmeta.getResourceCfgMap().get(Const.COLUMN_XLSX_SHEETLINELIMITS).toString());
        }
    }

    @Override
    public void beginWrite() throws IOException {
        super.beginWrite();
        try {
            XMLOutputFactory factory=XMLOutputFactory.newFactory();
            workbook = new XSSFWorkbook();
            Field field = workbook.getClass().getSuperclass().getDeclaredField("pkg");
            field.setAccessible(true);
            opcPackage = (OPCPackage) field.get(workbook);
            workbook.createSheet("sheet1");
            PackagePartName packagePartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet1.xml");
            opcPackage.removePart(packagePartName);
            for(int i=0;i<colmeta.getColumnList().size();i++){
                ExcelCellStyleUtil.getCellStyle(workbook, colmeta.getColumnList().get(i).getColumnType(), cellStyleMap);
            }
            byteOut=new ByteArrayOutputStream();
            if(!multipleSheets) {
                workbook.write(byteOut);
                workbook.close();
                zipOutputStream = new ZipOutputStream(out);
                writeOrigin(zipOutputStream,new ByteArrayInputStream(byteOut.toByteArray()));
            }else{
                String tmpPath=System.getProperty("java.io.tmpdir");
                tmpFileName=tmpPath+File.separator+System.currentTimeMillis()+".zip";
                tmpZipFile=new FileOutputStream(tmpFileName);
                zipOutputStream = new ZipOutputStream(tmpZipFile);
            }
            zipOutputStream.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            bufferedOutputStream=new BufferedOutputStream(zipOutputStream,81920);
            streamWriter = factory.createXMLStreamWriter(bufferedOutputStream,colmeta.getEncode());
            writePrefix();
            writeHeader(sheetProp.getColumnPropList().stream().map(ExcelColumnProp::getColumnName).collect(Collectors.toList()),cellStyleMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private void createSheet() throws Exception {
        int sheetNum=rowPos/MAX_LINE+1;
        workbook.createSheet("sheet"+sheetNum);
        PackagePartName packagePartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet"+sheetNum+".xml");
        opcPackage.removePart(packagePartName);
        zipOutputStream.putNextEntry(new ZipEntry("xl/worksheets/sheet"+sheetNum+".xml"));
        writePrefix();
        writeHeader(sheetProp.getColumnPropList().stream().map(ExcelColumnProp::getColumnName).collect(Collectors.toList()),cellStyleMap);
    }
    protected void writePrefix() throws XMLStreamException {
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
    }

    private void writeOrigin(ZipOutputStream outputStream,InputStream inStream){
        try(ZipInputStream zis=ZipInputStream.class.isAssignableFrom(inStream.getClass())?(ZipInputStream)inStream:new ZipInputStream(inStream)){
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                outputStream.putNextEntry(new ZipEntry(entry.getName()));
                byte[] buffer = new byte[8192];
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.closeEntry();
            }
        }catch (IOException ex){

        }
    }

    @Override
    public void finishWrite() throws IOException {
        try{
            streamWriter.writeEndElement();
            streamWriter.writeEndElement();
            if(bufferedOutputStream!=null){
                bufferedOutputStream.flush();
            }
            streamWriter.flush();
            if(!(out instanceof FinishableOutputStream)) {
                writer.flush();
            }else{
                ((FinishableOutputStream) out).finish();
            }

        }catch (Exception ex){
            throw new IOException(ex);
        }finally {
            try {
                if(streamWriter!=null) {
                    streamWriter.close();
                }
            }catch (XMLStreamException ex){
                ex.printStackTrace();
            }
            if(zipOutputStream!=null) {
                zipOutputStream.closeEntry();
                zipOutputStream.close();
                if(multipleSheets){
                    workbook.write(byteOut);
                    workbook.close();
                    bufferedOutputStream.close();
                    tmpZipFile.close();
                    try(ZipOutputStream zOut1=new ZipOutputStream(out);
                            ZipInputStream sheetIn=new ZipInputStream(new FileInputStream(tmpFileName))){
                        writeOrigin(zOut1,new ByteArrayInputStream(byteOut.toByteArray()));
                        writeOrigin(zOut1,sheetIn);
                    }catch (Exception ex){

                    }finally {
                        FileUtil.del(tmpFileName);
                    }
                }
            }
            if(bufferedOutputStream!=null) {
                bufferedOutputStream.close();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if(bufferedOutputStream!=null) {
            bufferedOutputStream.flush();
        }
    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        try{
            //数据量超过单sheet上限，需要新的Sheet
            if(currentSheetPos==MAX_LINE){
                if(!multipleSheets){
                    throw new OperationNotSupportedException("must set tag "+Const.COLUMN_XLSX_MULTIPLESHEETS);
                }
                streamWriter.writeEndElement();
                streamWriter.writeEndElement();
                streamWriter.flush();
                bufferedOutputStream.flush();
                zipOutputStream.closeEntry();
                createSheet();
                currentSheetPos=0;
            }
            writeLine(map,colmeta,cellStyleMap,currentSheetPos+2);
            rowPos++;
            currentSheetPos++;
        }catch (Exception ex){
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
    protected void writeLine(Map<String, Object> valueMap, DataCollectionMeta colmeta, Map<String, CellStyle> cellStyleMap, int rowpos) throws XMLStreamException {

        streamWriter.writeStartElement("row");
        streamWriter.writeAttribute("r", String.valueOf(rowpos));
        for (int i = 0; i < colmeta.getColumnList().size(); i++) {
            CellStyle style = cellStyleMap.get(colmeta.getColumnList().get(i).getColumnType());
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
        CellStyle style = cellStyleMap.get(Const.META_TYPE_STRING);
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
