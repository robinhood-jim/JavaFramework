package com.robin.comm.test;

import com.robin.comm.util.xls.*;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
public class TestExcelOperation {
    private Logger logger= LoggerFactory.getLogger(TestExcelOperation.class);
    @Test
    public void testGenerate() throws Exception {
        Long startTs = System.currentTimeMillis() - 3600 * 24 * 1000;
        String str = StringUtils.generateRandomChar(32);
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
        TableConfigProp header = new TableConfigProp();
        header.setContainrow(1);
        Random random = new Random(12312321321312L);
        AbstractResIterator iterator = new AbstractResIterator() {
            Map<String, Object> map = new HashMap<>();
            int row = 0;
            @Override
            public void init() {

            }

            @Override
            public void beforeProcess(String param) {

            }

            @Override
            public void afterProcess() {

            }
            @Override
            public void close() throws IOException {

            }
            @Override
            public boolean hasNext() {
                if (row < 500000)
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
                row++;
                return map;
            }

            @Override
            public String getIdentifier() {
                return "test";
            }
        };
        Workbook wb=ExcelProcessor.generateExcelFile(prop,header,iterator);
        FileOutputStream out=new FileOutputStream("d:/test.xlsx");
        wb.write(out);
        out.close();

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

        Connection conn = null;
        String sql = "select uuid,corp_id as corpId,car_num as carNum,vin,car_brand as brand,time_modified as modifier,car_color as color,manufacturer as maufactor,car_model as model,engine_type as engine,registcert_img1 as img from t_zhcx_car_vehiclelicence";
        try {
            //System.in.read();
            System.out.println("start");
            Long ts1 = System.currentTimeMillis();
            DataBaseParam param = new DataBaseParam("127.0.0.1", 3388, "root", "root", "1234");
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
            conn = SimpleJdbcDao.getConnection(meta);
            Workbook wb = ExcelProcessor.generateExcelFile(prop, header, conn, sql, null, new ExcelRsExtractor(prop, header));
            FileOutputStream out = new FileOutputStream("d:/test.xlsx");
            wb.write(out);
            out.close();
            ((SXSSFWorkbook) wb).dispose();
            System.out.println("--finish--" + String.valueOf(System.currentTimeMillis() - ts1));

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }

    }

    @Test
    public void processReadFile() {
        String filePath = "";
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.addColumnProp(new ExcelColumnProp("id", "id", Const.META_TYPE_BIGINT, false));
        prop.addColumnProp(new ExcelColumnProp("sno", "sno", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("car_num", "car_num", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("sendtime", "sendtime", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("gpstime", "gpstime", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("status", "status", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("desc", "desc", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("speed", "speed", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("forbidespeed", "forbidespeed", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("pos", "pos", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("longitude", "longitude", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("latitude", "latitude", Const.META_TYPE_DOUBLE, false));
        prop.addColumnProp(new ExcelColumnProp("alarm", "alarm", Const.META_TYPE_STRING, false));
        prop.addColumnProp(new ExcelColumnProp("other", "other", Const.META_TYPE_STRING, false));

        try {
            ExcelProcessor.readExcelFile(filePath, prop);
            List<Map<String, Object>> list = prop.getColumnList();
            System.out.println(list);
        } catch (Exception ex) {

        }
    }
    @Test
    public void doRead(){
        DateTimeFormatter format=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try(InputStream stream=new FileInputStream(new File("e:/2022级高一10月校考质量分析表.xls"))){
            ExcelProcessor.readExcel(stream,"xls",1,2,(w,r,ev)->{
                Object cValue=ExcelProcessor.readValue(r.getCell(10),Const.META_TYPE_DOUBLE,format,ev);
                logger.info("{} {}",cValue,cValue.getClass());
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Test
    public void testMergeHeader(){

        ExcelSheetProp prop=new ExcelSheetProp("xlsx");

        prop.setStartCol(1);
        prop.setStartRow(2);
        prop.setSheetName("TestSheet1");
        TableConfigProp configProp=new TableConfigProp();
        List<List<TableHeaderColumn>> headList=new ArrayList<>();
        List<TableHeaderColumn> list1=new ArrayList<>();
        list1.add(new TableHeaderColumn("班级","gradeNo",2,1));
        list1.add(new TableHeaderColumn("10月月考","Oct",1,3));
        list1.add(new TableHeaderColumn("11月月考","Nov",1,3));
        list1.add(new TableHeaderColumn("期末统考","Last",1,3));
        headList.add(list1);
        List<TableHeaderColumn> list2=new ArrayList<>();
        list2.add(new TableHeaderColumn("平级分","OctAvg",1,1));
        list2.add(new TableHeaderColumn("名称","OctRank",1,1));
        list2.add(new TableHeaderColumn("均分差","OctMinus",1,1));
        list2.add(new TableHeaderColumn("平级分","NovAvg",1,1));
        list2.add(new TableHeaderColumn("名称","NovRank",1,1));
        list2.add(new TableHeaderColumn("均分差","NovMinus",1,1));
        list2.add(new TableHeaderColumn("平级分","LastAvg",1,1));
        list2.add(new TableHeaderColumn("名称","LastRank",1,1));
        list2.add(new TableHeaderColumn("均分差","LastMinus",1,1));
        headList.add(list2);
        configProp.setHeaderColumnList(headList);
        configProp.setTotalCol(10);


        try(OutputStream outputStream=new FileOutputStream(new File("e:/test2.xlsx"));
            Workbook wb=ExcelProcessor.generateExcelFile(prop,configProp)){
            wb.write(outputStream);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    @Test
    public void doReadMergeCells(){
        try(Workbook wb=new XSSFWorkbook(new FileInputStream(new File("e:/1234.xlsx")))){
            List<Triple<Integer,Integer,List<Object>>> rList=ExcelBaseOper.getMergedCells(wb.getSheetAt(0),2,1,new int[]{2,3});
            log.info("",rList);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    @Test
    public void doTestRead(){
        try(Workbook wb = new XSSFWorkbook(new FileInputStream(new File("e:/test1.xlsx")))){
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            ExcelProcessor.readExcel(wb,"xlsx",0,2,(w,r,ev)->{
                for(int i=0;i<3;i++){
                    Object obj=ExcelProcessor.readValue(r.getCell(i),Const.META_TYPE_DOUBLE,format,ev);
                    System.out.println(obj);
                }
            });
        }catch (Exception ex){

        }
    }
    @Test
    public void doGen1(){
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
        List<Map<String,Object>> rsList=new ArrayList<>();
        Map<String,Object> tmap=new HashMap<>();
        tmap.put("avg",85.0);
        tmap.put("rank",1);
        tmap.put("minus",-0.9);
        tmap.put("className","test123");
        rsList.add(tmap);
        sheetProp.setColumnList(rsList);
        try(Workbook wb=new XSSFWorkbook()){
            Sheet fSheet=wb.createSheet("test");
            ExcelProcessor.fillSheet(fSheet,sheetProp,tableConfigProp);
            wb.write(new FileOutputStream(new File("e:/1234.xlsx")));
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
