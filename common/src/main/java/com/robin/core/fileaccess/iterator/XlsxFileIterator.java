package com.robin.core.fileaccess.iterator;

import com.google.common.collect.Sets;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.PolandNotationUtil;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;
/**
 * read xlsx with StAX api
 */
public class XlsxFileIterator extends AbstractFileIterator{
    private XMLInputFactory factory;
    private XMLStreamReader streamReader;
    private OPCPackage opcPackage;
    private Iterator<InputStream> sheetStreams;
    private InputStream readStreams;
    private XSSFReader xssfReader;
    private SharedStrings sharedStrings;
    private static final char charA='A';
    private static boolean hasHeader=true;
    private Map<String,String> formulaStrMap=new HashMap<>();
    private Map<String, Queue<String>> formulaMap=new HashMap<>();
    private static final Set<Character> formulaSets= Sets.newHashSet('(',')','+','-','*','/');

    public XlsxFileIterator(){
        identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }

    public XlsxFileIterator(DataCollectionMeta meta) {
        super(meta);
        identifier= Const.FILEFORMATSTR.XLSX.getValue();
    }
    public XlsxFileIterator(DataCollectionMeta meta, AbstractFileSystemAccessor accessor) {
        super(meta,accessor);
        identifier= Const.FILEFORMATSTR.XLSX.getValue();
        if(meta.getResourceCfgMap().containsKey(Const.COLUMN_XLSX_HASHEADER) && Const.FALSE.equalsIgnoreCase(meta.getResourceCfgMap().get(Const.COLUMN_XLSX_HASHEADER).toString())){
            hasHeader=false;
        }
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
        try {
            factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            opcPackage=OPCPackage.open(instream);
            xssfReader=new XSSFReader(opcPackage);
            sheetStreams=xssfReader.getSheetsData();
            sharedStrings=xssfReader.getSharedStringsTable();
            if(sheetStreams.hasNext()){
                readStreams=sheetStreams.next();
                streamReader=factory.createXMLStreamReader(readStreams,colmeta.getEncode());
                String nodeName;
                while(streamReader.hasNext()){
                    if(streamReader.getEventType()== XMLStreamConstants.START_ELEMENT) {
                        nodeName = streamReader.getLocalName();
                        if("row".equalsIgnoreCase(nodeName)){
                            break;
                        }
                    }
                    streamReader.next();
                }
            }
            if(hasHeader){
                readNext();
            }
        }catch (Exception ex){

        }
    }
    private void readNext() throws Exception{
        String nodeName;
        int rowNum;
        String rowNumStr;
        int columnPos=0;
        String value;
        boolean sharedValue=false;
        boolean breakTag=false;
        Object targetValue=null;


        while (streamReader.hasNext() && !breakTag){
            if(streamReader.getEventType()== XMLStreamConstants.START_ELEMENT){
                nodeName=streamReader.getLocalName();
                switch (nodeName.toLowerCase()){
                    case "row":
                        rowNumStr = streamReader.getAttributeValue("", "r");
                        rowNum = Integer.parseInt(rowNumStr);
                        break;
                    case "c":
                        columnPos=getColumnPos(streamReader.getAttributeValue("","r"))-1;
                        String cellType=streamReader.getAttributeValue("","t");
                        if("s".equals(cellType)){
                            sharedValue=true;
                        }
                        break;
                    case "v":
                        streamReader.next();
                        value=streamReader.getText();
                        if(sharedValue){
                            targetValue=new XSSFRichTextString(sharedStrings.getItemAt(Integer.parseInt(value)).getString()).toString();
                            sharedValue=false;
                        }else{
                            targetValue=parseValue(value, colmeta.getColumnList().get(columnPos));
                        }
                        break;
                    case "t":
                        streamReader.next();
                        targetValue=streamReader.getText();
                        break;
                    case "f":
                        streamReader.next();
                        if(streamReader.getEventType()==XMLStreamConstants.CHARACTERS) {
                            value = streamReader.getText();

                        }
                        break;
                }

            }else if(streamReader.getEventType()==XMLStreamConstants.END_ELEMENT){
                nodeName=streamReader.getLocalName();
                switch (nodeName.toLowerCase()) {
                    case "row":
                        breakTag=true;
                        break;
                    case "t":
                    case "v":
                        cachedValue.put(colmeta.getColumnList().get(columnPos).getColumnName(),targetValue);
                        break;
                    case "f":
                        /*if(!formulaStrMap.containsKey(colmeta.getColumnList().get(columnPos).getColumnName()) || !formulaStrMap.get(colmeta.getColumnList().get(columnPos).getColumnName()).equals(value)){
                            formulaStrMap.put(colmeta.getColumnList().get(columnPos).getColumnName(),value);
                            Queue<String> queue= PolandNotationUtil.parsePre(getFormula(value));
                            if(!CollectionUtils.isEmpty(queue)){
                                formulaMap.put(colmeta.getColumnList().get(columnPos).getColumnName(),queue);
                            }
                        }*/
                        break;

                }
            }
            streamReader.next();
        }
    }
    private Object parseValue(String value, DataSetColumnMeta columnMeta){
        if(!columnMeta.getColumnType().equals(Const.META_TYPE_TIMESTAMP)){
           return ConvertUtil.convertStringToTargetObject(value, columnMeta, formatter);
        }else{
            double dValue=Double.valueOf(value);
            Date date = DateUtil.getJavaDate(dValue);
            return new Timestamp(date.getTime());
        }
    }
    private static int getColumnPos(String columnName){
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<columnName.length();i++){
            if(!Character.isAlphabetic(columnName.charAt(i))){
                builder.append(columnName.substring(0,i));
                break;
            }
        }
        int columnPos=0;
        if(builder.length()>0){
            for(int i=0;i<builder.length();i++){
                int val=(builder.charAt(i)-charA)+1;
                if(i>0){
                    columnPos=columnPos*26+val;
                }else{
                    columnPos=val;
                }
            }
        }
        return columnPos;
    }
    private String getFormula(String formula){
        StringBuilder builder=new StringBuilder();
        StringBuilder outBuilder=new StringBuilder();
        for(int i=0;i<formula.length();i++){
            if(formulaSets.contains(formula.charAt(i))){
                if(builder.length()>0){
                    String columnName=builder.toString();
                    outBuilder.append(colmeta.getColumnList().get(getColumnPos(columnName)).getColumnName());
                    builder.delete(0,builder.length());
                }
                outBuilder.append(formula.charAt(i));
            }else{
                builder.append(formula.charAt(i));
            }
        }
        if(formulaSets.contains(builder.charAt(0))){
            outBuilder.append(builder.charAt(0));
        }else{
            String columnName=builder.toString();
            outBuilder.append(colmeta.getColumnList().get(getColumnPos(columnName)).getColumnName());
        }
        return outBuilder.toString();
    }

    @Override
    protected void pullNext() throws Exception {
        cachedValue.clear();
        readNext();
        if(CollectionUtils.isEmpty(cachedValue)){
            if(sheetStreams.hasNext()){
                readStreams.close();
                streamReader.close();
                readStreams=sheetStreams.next();
                streamReader=factory.createXMLStreamReader(readStreams,colmeta.getEncode());
                if(hasHeader) {
                    readNext();
                }
            }
            readNext();
        }
        //calculate formula column
        if(!formulaMap.isEmpty()){
            formulaMap.forEach((k,v)->{
                if(ObjectUtils.isEmpty(cachedValue.get(k))){
                    cachedValue.put(k,PolandNotationUtil.computeResult(v,cachedValue));
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if(readStreams!=null){
            readStreams.close();
        }
        if(opcPackage!=null){
            opcPackage.close();
        }
    }
}
