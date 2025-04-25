package com.robin.comm.test;

import com.robin.comm.util.xls.*;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.fs.LocalFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.iterator.TextFileIteratorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import com.robin.core.fileaccess.writer.TextFileWriterFactory;
import com.robin.core.fileaccess.writer.XlsxFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.openxml4j.opc.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipOutputStream;


@Slf4j
public class TestExcelOperation {
    private Logger logger = LoggerFactory.getLogger(TestExcelOperation.class);
    private static final char CHARA = 'A';

    @Test
    public void testReadWithIterator() throws Exception {
        DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();
        builder.addColumn("name", Const.META_TYPE_STRING).addColumn("time", Const.META_TYPE_TIMESTAMP)
                .addColumn("intcol", Const.META_TYPE_INTEGER).addColumn("dval", Const.META_TYPE_DOUBLE)
                .addColumn("dval2", Const.META_TYPE_DOUBLE).addColumn("diff", Const.META_TYPE_DOUBLE)
                .fileFormat(Const.FILEFORMATSTR.XLSX.getValue()).resPath("file:///d:/test.xlsx");

        LocalFileSystemAccessor accessor = LocalFileSystemAccessor.getInstance();
        try (AbstractFileIterator iterator = (AbstractFileIterator) TextFileIteratorFactory.getProcessIteratorByType(builder.build(), accessor)) {
            int pos = 0;
            while (iterator.hasNext()) {
                pos++;
                if (pos % 10000 == 0) {
                    System.out.println(iterator.next());
                }
            }
            System.out.println(pos);
        } catch (IOException ex) {

        }
    }
    @Test
    public void testWriteStAX() throws Exception{
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.addColumnProp(new ExcelColumnProp("name", "name", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("time", "time", Const.META_TYPE_TIMESTAMP, false));
        prop.addColumnProp(new ExcelColumnProp("intcol", "intcol", Const.META_TYPE_INTEGER, false));
        prop.addColumnProp(new ExcelColumnProp("dval", "dval", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("dval2", "dval2", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("diff", "diff", Const.META_TYPE_FORMULA, "(D{P}-E{P})/C{P}"));
        DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();
        builder.addColumn("name", Const.META_TYPE_STRING).addColumn("time", Const.META_TYPE_TIMESTAMP)
                .addColumn("intcol", Const.META_TYPE_INTEGER).addColumn("dval", Const.META_TYPE_DOUBLE)
                .addColumn("dval2", Const.META_TYPE_DOUBLE).addColumn("diff", Const.META_TYPE_DOUBLE)
                .fileFormat(Const.FILEFORMATSTR.XLSX.getValue()).resPath("file:///d:/test2.xlsx");
        DataCollectionMeta meta = builder.build();
        LocalFileSystemAccessor accessor = LocalFileSystemAccessor.getInstance();
        Random random = new Random(12312321321312L);
        Map<String,Object> cachedMap=new HashMap<>();
        try(XlsxFileWriter writer=(XlsxFileWriter) TextFileWriterFactory.getWriterByType(meta,accessor)){
            writer.setSheetProp(prop);
            writer.beginWrite();
            Long startTs = System.currentTimeMillis() - 3600 * 24 * 1000;
            for(int j=0;j<500000;j++){
                cachedMap.put("name", StringUtils.generateRandomChar(12));
                cachedMap.put("time", String.valueOf(startTs + j * 1000));
                cachedMap.put("intcol", String.valueOf(random.nextInt(1000)));
                cachedMap.put("dval", String.valueOf(random.nextDouble() * 1000));
                cachedMap.put("dval2", String.valueOf(random.nextDouble() * 500));
                writer.writeRecord(cachedMap);
            }
            writer.finishWrite();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Test
    public void testWriteStream() throws Exception{
        DataCollectionMeta.Builder builder = new DataCollectionMeta.Builder();
        builder.addColumn("name", Const.META_TYPE_STRING).addColumn("time", Const.META_TYPE_TIMESTAMP)
                .addColumn("intcol", Const.META_TYPE_INTEGER).addColumn("dval", Const.META_TYPE_DOUBLE)
                .addColumn("dval2", Const.META_TYPE_DOUBLE).addColumn("diff", Const.META_TYPE_DOUBLE)
                .fileFormat(Const.FILEFORMATSTR.XLSX.getValue()).resPath("file:///d:/test.xlsx");
        DataCollectionMeta meta = builder.build();
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter writer=null;
        Map<String,CellStyle> cellStyleMap=new HashMap<>();
        Map<String,Object> cachedMap=new HashMap<>();
        Random random = new Random(12312321321312L);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
             Field field=workbook.getClass().getSuperclass().getDeclaredField("pkg");
             field.setAccessible(true);
             OPCPackage opcPackage=(OPCPackage) field.get(workbook);
             workbook.createSheet("sheet1");

            List<PackagePart> sheets = opcPackage.getPartsByContentType(XSSFRelation.WORKSHEET.getContentType());

            List<PackagePart> sharedParts = opcPackage.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());

            PackagePartName packagePartName = PackagingURIHelper.createPartName("/xl/worksheets/sheet1.xml");
            opcPackage.removePart(packagePartName);
            //opcPackage.deletePart(packagePartName);
            //PackagePart part=opcPackage.getPart(packagePartName);
            PackagePart part = opcPackage.createPart(packagePartName,XSSFRelation.WORKSHEET.getContentType());
            opcPackage.addRelationship(packagePartName, TargetMode.INTERNAL,XSSFRelation.WORKSHEET.getRelation());
            opcPackage.flush();
            //ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
            OutputStream outputStream = part.getOutputStream();
            writer = factory.createXMLStreamWriter(outputStream);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("worksheet");
            writer.writeAttribute("xmlxs", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
            writer.writeEmptyElement("dimension");
            writer.writeAttribute("ref", "A1:" + getEndCell(meta.getColumnList().size()) + "50001");
            writer.writeStartElement("sheetViews");
            writer.writeStartElement("sheetView");
            writer.writeAttribute("workbookViewId", "0");
            writer.writeAttribute("tabSelected", "true");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEmptyElement("sheetFormatPr");
            writer.writeAttribute("defaultRowHeight", "15.0");
            writer.writeStartElement("cols");
            writer.writeStartElement("col");
            writer.writeAttribute("min", "1");
            writer.writeAttribute("max", "1");
            writer.writeAttribute("customWidth", "true");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement("sheetData");
            writeHeader(writer,workbook,Arrays.asList("name","time","intcol","dval","dval2","diff"),cellStyleMap);
            cachedMap.clear();
            Long startTs = System.currentTimeMillis() - 3600 * 24 * 1000;
            for(int j=0;j<5000;j++){
                cachedMap.put("name", StringUtils.generateRandomChar(12));
                cachedMap.put("time", String.valueOf(startTs + j * 1000));
                cachedMap.put("intcol", String.valueOf(random.nextInt(1000)));
                cachedMap.put("dval", String.valueOf(random.nextDouble() * 1000));
                cachedMap.put("dval2", String.valueOf(random.nextDouble() * 500));
                writeLine(writer,workbook,cachedMap,meta,cellStyleMap,j+2);
            }
            writer.writeEndElement();
            writer.writeEndElement();
            writer.flush();
            writer.close();
            part.flush();
            opcPackage.flush();
            Sheet sheet=workbook.getSheetAt(0);
            for(int i=0;i<meta.getColumnList().size();i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(new FileOutputStream("d:/test1.xlsx"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(writer!=null) {
                writer.close();
            }
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

    private void writeLine(XMLStreamWriter writer, Workbook wb, Map<String, Object> valueMap, DataCollectionMeta colmeta, Map<String, CellStyle> cellStyleMap, int rowpos) throws Exception {

        writer.writeStartElement("row");
        writer.writeAttribute("r", String.valueOf(rowpos));
        for (int i = 0; i < colmeta.getColumnList().size(); i++) {
            CellStyle style = ExcelCellStyleUtil.getCellStyle(wb, colmeta.getColumnList().get(i).getColumnType(), cellStyleMap);
            if (!ObjectUtils.isEmpty(valueMap.get(colmeta.getColumnList().get(i).getColumnName()))) {
                writer.writeStartElement("c");
                writer.writeAttribute("r", getEndCell(i + 1) + rowpos);
                writer.writeAttribute("s", String.valueOf(style.getFontIndexAsInt()));
                if (Const.META_TYPE_STRING.equals(colmeta.getColumnList().get(i).getColumnType())) {
                    writer.writeAttribute("t", "inlineStr");
                    writer.writeStartElement("is");
                    writer.writeStartElement("t");
                    writer.writeCharacters(valueMap.get(colmeta.getColumnList().get(i).getColumnName()).toString());
                    writer.writeEndElement();
                    writer.writeEndElement();
                } else {
                    writer.writeAttribute("t", "n");
                    writer.writeStartElement("v");
                    if (Const.META_TYPE_TIMESTAMP.equals(colmeta.getColumnList().get(i).getColumnType()) || Const.META_TYPE_DATE.equals(colmeta.getColumnList().get(i).getColumnType())) {
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
                        writer.writeCharacters(String.valueOf(dateVal));
                    } else {
                        writer.writeCharacters(valueMap.get(colmeta.getColumnList().get(i).getColumnName()).toString());
                    }
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private void writeHeader(XMLStreamWriter writer, Workbook wb, List<String> columnName, Map<String, CellStyle> cellStyleMap) throws Exception {
        CellStyle style = ExcelCellStyleUtil.getCellStyle(wb, Const.META_TYPE_STRING, cellStyleMap);
        writer.writeStartElement("row");
        writer.writeAttribute("r", "1");
        for (int i = 0; i < columnName.size(); i++) {
            writer.writeStartElement("c");
            writer.writeAttribute("r", getEndCell(i + 1) + "1");
            writer.writeAttribute("s", String.valueOf(style.getIndex()));
            writer.writeAttribute("t", "inlineStr");
            writer.writeStartElement("is");
            writer.writeStartElement("t");
            writer.writeCharacters(columnName.get(i));
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    @Test
    public void testGen1() throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        XMLStreamWriter writer = null;

        try {
            writer = factory.createXMLStreamWriter(new FileOutputStream("d:/test1.xml"), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("worksheet");
            writer.writeAttribute("xmlxs", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
            writer.writeEmptyElement("dimension");
            writer.writeAttribute("ref", "A1:D13");
            writer.writeStartElement("sheetViews");
            writer.writeStartElement("sheetView");
            writer.writeAttribute("workbookViewId", "0");
            writer.writeAttribute("tabSelected", "true");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEmptyElement("sheetFormatPr");
            writer.writeAttribute("defaultRowHeight", "15.0");
            writer.writeStartElement("cols");
            writer.writeStartElement("col");
            writer.writeAttribute("min", "1");
            writer.writeAttribute("max", "1");
            writer.writeAttribute("customWidth", "true");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement("sheetData");
            writer.writeStartElement("row");
            writer.writeAttribute("r", "1");
            writer.writeCharacters("\n");
            writer.writeStartElement("c");
            writer.writeAttribute("r", "A1");
            writer.writeAttribute("s", "1");
            writer.writeAttribute("t", "n");
            writer.writeStartElement("v");
            writer.writeCharacters("111.0");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement("c");
            writer.writeAttribute("r", "B1");
            writer.writeAttribute("s", "1");
            writer.writeAttribute("t", "n");
            writer.writeCharacters("200.0");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }

    @Test
    public void testGenerate() throws Exception {
        Long startTs = System.currentTimeMillis() - 3600 * 24 * 1000;
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.setFileExt("xlsx");
        prop.setStartCol(1);
        prop.setStartRow(1);
        prop.setSheetName("test");
        prop.setStreamInsert(true);
        prop.addColumnProp(new ExcelColumnProp("name", "name", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("time", "time", Const.META_TYPE_TIMESTAMP, false));
        prop.addColumnProp(new ExcelColumnProp("intcol", "intcol", Const.META_TYPE_INTEGER, false));
        prop.addColumnProp(new ExcelColumnProp("dval", "dval", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("dval2", "dval2", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("diff", "diff", Const.META_TYPE_FORMULA, "(D{P}-E{P})/C{P}"));
        //prop.addColumnProp(new ExcelColumnProp("差距1", "sep1", Const.META_TYPE_FORMULA, "D{P+1}-D{P}"));
        //prop.addColumnProp(new ExcelColumnProp("差距2", "sep2", Const.META_TYPE_FORMULA, "D{P-1}-D{P}"));
        TableConfigProp header = new TableConfigProp();
        header.setContainrow(1);
        Random random = new Random(12312321321312L);
        AbstractResIterator iterator = new AbstractResIterator() {
            Map<String, Object> map = new HashMap<>();
            int row = 0;

            @Override
            public void beforeProcess() {

            }

            @Override
            public void afterProcess() {

            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public boolean hasNext() {
                if (row < 50000)
                    return true;
                return false;
            }

            @Override
            public Map<String, Object> next() {
                map.clear();
                map.put("name", StringUtils.generateRandomChar(12));
                map.put("time", String.valueOf(startTs + row * 1000));
                map.put("intcol", String.valueOf(random.nextInt(1000)));
                map.put("dval", String.valueOf(random.nextDouble() * 1000));
                map.put("dval2", String.valueOf(random.nextDouble() * 500));
                row++;
                return map;
            }

            @Override
            public String getIdentifier() {
                return "test";
            }
        };
        Workbook wb = ExcelProcessor.generateExcelFile(prop, header, iterator);
        FileOutputStream out = new FileOutputStream("d:/test.xlsx");
        wb.write(out);
        out.close();

    }

    @Test
    public void testRead() throws IOException {
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.setFileExt("xlsx");
        prop.setStartCol(1);
        prop.setStartRow(1);
        prop.setSheetName("test");
        prop.setStreamInsert(true);
        prop.addColumnProp(new ExcelColumnProp("name", "name", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("time", "time", Const.META_TYPE_TIMESTAMP, false));
        prop.addColumnProp(new ExcelColumnProp("intcol", "intcol", Const.META_TYPE_INTEGER, false));
        prop.addColumnProp(new ExcelColumnProp("dval", "dval", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("差距1", "sep1", Const.META_TYPE_FORMULA, "D{P+1}-D{P}"));
        prop.addColumnProp(new ExcelColumnProp("差距2", "sep2", Const.META_TYPE_FORMULA, "D{P-1}-D{P}"));
        ExcelProcessor.readExcelFile("d:/test.xlsx", prop);
        if (!CollectionUtils.isEmpty(prop.getColumnList())) {
            prop.getColumnList().forEach(f -> System.out.println(f));
        }
    }

    @Test
    public void testGenWithQuery() {
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.setFileExt("xlsx");
        prop.setStartCol(1);
        prop.setStartRow(1);
        prop.setSheetName("TestSheet1");
        prop.addColumnProp(new ExcelColumnProp("唯一键", "uuid", Const.META_TYPE_BIGINT, false));
        prop.addColumnProp(new ExcelColumnProp("企业id", "corpId", Const.META_TYPE_BIGINT, false));
        prop.addColumnProp(new ExcelColumnProp("车牌号", "carNum", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("车架号", "vin", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("品牌", "brand", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("修改时间", "modifier", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("颜色", "color", Const.META_TYPE_BIGINT, false));
        prop.addColumnProp(new ExcelColumnProp("生产者", "maufactor", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("model", "model", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("engine", "engine", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("img", "img", Const.META_TYPE_STRING, false));
        prop.setStreamInsert(true);
        prop.setStreamRows(3000);
        TableConfigProp header = new TableConfigProp();
        header.setContainrow(1);
        header.setContentFontName("微软雅黑");


        String sql = "select uuid,corp_id as corpId,car_num as carNum,vin,car_brand as brand,time_modified as modifier,car_color as color,manufacturer as maufactor,car_model as model,engine_type as engine,registcert_img1 as img from t_licence";
        DataBaseParam param = new DataBaseParam("127.0.0.1", 3316, "test", "test", "test");
        BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
        try (Connection conn = SimpleJdbcDao.getConnection(meta)) {
            Workbook wb = ExcelProcessor.generateExcelFile(prop, header, conn, sql, null, new ExcelRsExtractor(prop, header));
            FileOutputStream out = new FileOutputStream("d:/test.xlsx");
            wb.write(out);
            out.close();
            ((SXSSFWorkbook) wb).dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void doTestRead() {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream("e:/1234.xlsx"))) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            ExcelProcessor.readExcel(wb, 0, 2, (w, r, ev) -> {
                Object obj = ExcelProcessor.readValue(r.getCell(1), Const.META_TYPE_DOUBLE, format, ev);
                System.out.println(obj);
            });
        } catch (Exception ex) {

        }
    }


    @Test
    public void doGenMergeHeader() {
        List<List<TableHeaderColumn>> headList = new ArrayList<>();
        List<TableHeaderColumn> firstCols = new ArrayList<>();
        List<TableHeaderColumn> secondCols = new ArrayList<>();
        TableConfigProp tableConfigProp = new TableConfigProp();
        tableConfigProp.setTotalCol(4);

        ExcelSheetProp sheetProp = new ExcelSheetProp("xlsx");
        firstCols.add(new TableHeaderColumn("班级", "gradeNo", 2, 1));
        firstCols.add(new TableHeaderColumn("考试名次", "name", 1, 3));
        secondCols.add(new TableHeaderColumn("平级分", "avg", 1, 1));
        secondCols.add(new TableHeaderColumn("名次", "rank", 1, 1));
        secondCols.add(new TableHeaderColumn("均分差", "minus", 1, 1));
        headList.add(firstCols);
        headList.add(secondCols);
        tableConfigProp.setHeaderColumnList(headList);
        sheetProp.addColumnProp(new ExcelColumnProp("班级", "className", Const.META_TYPE_STRING));
        sheetProp.addColumnProp(new ExcelColumnProp("平级分", "avg", Const.META_TYPE_DOUBLE));
        sheetProp.addColumnProp(new ExcelColumnProp("名次", "rank", Const.META_TYPE_INTEGER));
        sheetProp.addColumnProp(new ExcelColumnProp("均分差", "minus", Const.META_TYPE_DOUBLE));
        List<Map<String, Object>> rsList = new ArrayList<>();
        Map<String, Object> tmap = new HashMap<>();
        tmap.put("avg", 85.0);
        tmap.put("rank", 1);
        tmap.put("minus", -0.9);
        tmap.put("className", "test123");
        rsList.add(tmap);
        sheetProp.setColumnList(rsList);
        try (Workbook wb = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream("e:/1234.xlsx")) {
            Sheet fSheet = wb.createSheet("test");
            ExcelProcessor.fillSheet(fSheet, sheetProp, tableConfigProp);
            wb.write(outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
