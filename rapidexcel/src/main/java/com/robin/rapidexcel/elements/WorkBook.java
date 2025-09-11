package com.robin.rapidexcel.elements;

import cn.hutool.core.io.FileUtil;
import com.robin.comm.util.xls.ExcelSheetProp;
import com.robin.rapidexcel.reader.XMLReader;
import com.robin.rapidexcel.utils.MapSpliterator;
import com.robin.rapidexcel.utils.OPCPackage;
import com.robin.rapidexcel.utils.RowSpliterator;
import com.robin.rapidexcel.utils.XMLFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipOutputStream;

public class WorkBook implements Closeable {
    private Map<Integer, CellType> styleMap;
    private int activeTab = 0;
    private boolean finished = false;
    private String applicationName="com.robin FastExcel";
    private String applicationVersion;
    private ZipOutputStream out;
    private OPCPackage opcPackage;
    private List<WorkSheet> sheets=new ArrayList<>();
    private Map<String, WorkSheet> sheetMap=new HashMap<>();
    private boolean date1904;
    private ExcelSheetProp prop;
    private ShardingStrings shardingStrings;

    public WorkBook(File file,String applicationName,String applicationVersion){
        this.applicationName=applicationName;
        this.applicationVersion=applicationVersion;
        opcPackage=OPCPackage.create(file);
    }

    public WorkBook(OutputStream cout,String applicationName,String applicationVersion){
        this.out=new ZipOutputStream(cout);
        this.applicationName=applicationName;
        this.applicationVersion=applicationVersion;
        opcPackage=OPCPackage.create(out);
    }
    public WorkBook(InputStream inputStream) throws XMLStreamException,IOException{
        Assert.notNull(inputStream,"");
        opcPackage=OPCPackage.open(inputStream);
        beginRead();
    }
    public WorkBook(File file) throws XMLStreamException,IOException {
        if(!FileUtil.exist(file)){
            throw new IOException("file not found!");
        }
        opcPackage=OPCPackage.open(file);
        beginRead();
    }
    public void setSheetProp(ExcelSheetProp prop){
        this.prop=prop;
    }

    public ExcelSheetProp getSheetProp() {
        return prop;
    }

    public List<String> getFormats(){
        return opcPackage.getFormatIdList();
    }
    public Map<String,String> getNumFmtMap(){
        return opcPackage.getFormatMap();
    }

    private void beginRead() throws XMLStreamException,IOException {
        try(XMLReader reader=new XMLReader(XMLFactoryUtils.getDefaultInputFactory(),opcPackage.getWorkBookContent())){
            while(reader.goTo(() -> reader.isStartElement("sheets") || reader.isStartElement("workbookPr") ||
                    reader.isStartElement("workbookView") || reader.isEndElement("workbook"))){
                if ("workbookView".equals(reader.getLocalName())) {
                    String activeTab = reader.getAttribute("activeTab");
                    if (activeTab != null) {
                        this.activeTab = Integer.parseInt(activeTab);
                    }
                } else if ("sheets".equals(reader.getLocalName())) {
                    reader.forEach("sheet", "sheets", this::createSheet);
                } else if ("workbookPr".equals(reader.getLocalName())) {
                    String date1904Value = reader.getAttribute("date1904");
                    date1904 = Boolean.parseBoolean(date1904Value);
                } else {
                    break;
                }
            }
        }
        shardingStrings=ShardingStrings.formInputStream(opcPackage.getShardingStrings());
    }
    private void createSheet(XMLReader r){
        String name = r.getAttribute("name");
        String id = r.getAttribute("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
        String sheetId = r.getAttribute("sheetId");
        SheetVisibility sheetVisibility;
        if ("veryHidden".equals(r.getAttribute("state"))) {
            sheetVisibility = SheetVisibility.VERY_HIDDEN;
        } else if ("hidden".equals(r.getAttribute("state"))) {
            sheetVisibility = SheetVisibility.HIDDEN;
        } else {
            sheetVisibility = SheetVisibility.VISIBLE;
        }
        int index = sheets.size();
        WorkSheet sheet=new WorkSheet(this,index,id,sheetId,name,sheetVisibility);
        sheets.add(sheet);
        sheetMap.put(name,sheet);
    }
    public Stream<Row> openStream(WorkSheet sheet) throws IOException{
        try{
            InputStream inputStream=opcPackage.getSheetContent(sheet);
            Stream<Row> stream = StreamSupport.stream(new RowSpliterator(this, inputStream,prop), false);
            return stream.onClose(asUncheckedRunnable(inputStream));
        }catch (XMLStreamException ex){
            throw new IOException(ex);
        }
    }
    public Stream<Map<String,Object>> openMapStream(WorkSheet sheet) throws IOException{
        try{
            InputStream inputStream=opcPackage.getSheetContent(sheet);
            Stream<Map<String,Object>> stream = StreamSupport.stream(new MapSpliterator(this, inputStream,prop), false);
            return stream.onClose(asUncheckedRunnable(inputStream));
        }catch (XMLStreamException ex){
            throw new IOException(ex);
        }
    }
    private static Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    public void finish(){

    }
    public ShardingStrings getShardingStrings(){
        return shardingStrings;
    }

    public boolean isDate1904() {
        return date1904;
    }
    public Optional<WorkSheet> getSheet(int index) {
        return index < 0 || index >= sheets.size() ? Optional.empty() : Optional.of(sheets.get(index));
    }
    public int getSheetNum(){
        return !ObjectUtils.isEmpty(sheets)?sheets.size():0;
    }

    public WorkSheet getFirstSheet() {
        return sheets.get(0);
    }

    public Optional<WorkSheet> findSheet(String name) {
        return sheets.stream().filter(sheet -> name.equals(sheet.getName())).findFirst();
    }

    @Override
    public void close() throws IOException {
        if(opcPackage!=null){
            opcPackage.close();
        }
        if(out!=null){
            out.closeEntry();
            out.close();
        }
    }

    public static class Builder{
        private static Builder builder;
        private static WorkBook workBook;
        private boolean useWriter=false;
        private static String applicationName;
        private String applicationVersion;
        private static InputStream inputStream;
        private File file;
        private OutputStream outputStream;

        private Builder(){

        }
        public static Builder newBuilder(){
            builder=new Builder();
            return builder;
        }
        public Builder readWithInputStream(InputStream stream){
            this.inputStream=stream;
            return this;
        }
        public Builder readWithFile(File file){
            this.file=file;
            return this;
        }
        public Builder applicationName(String applicationName){
            this.applicationName=applicationName;
            return this;
        }
        public Builder applicationVersion(String applicationVersion){
            this.applicationVersion=applicationVersion;
            return this;
        }



    }

}
