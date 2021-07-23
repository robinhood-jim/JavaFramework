package com.robin.comm.test;

import com.robin.comm.util.xls.*;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

public class TestExcelOperation {

    @Test
    public void testGenerate() throws Exception {
        Long startTs = System.currentTimeMillis() - 3600 * 24 * 1000;
        String str = StringUtils.generateRandomChar(32);
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.setFileext("xlsx");
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
            public void beforeProcess(String resourcePath) {

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
        };
        Workbook wb=ExcelProcessor.generateExcelFile(prop,header,iterator);
        FileOutputStream out=new FileOutputStream("d:/test.xlsx");
        wb.write(out);
        out.close();
        //List<Map<String, String>> list = new ArrayList<Map<String, String>>();


        //prop.setColumnList(list);
        //ExcelProcessor.generateExcelFileToLocal(prop, header, "d:/test1.xlsx");

    }

    @Test
    public void testGenWithQuery() {
        ExcelSheetProp prop = new ExcelSheetProp();
        prop.setFileext("xlsx");
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
            DataBaseParam param = new DataBaseParam("172.16.200.218", 3388, "awardsys2", "awardsys", "MiCUWcYcJI2EcM1k");
            BaseDataBaseMeta meta = DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL, param);
            conn = SimpleJdbcDao.getConnection(meta, param);
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
            List<Map<String, String>> list = prop.getColumnList();
            System.out.println(list);
        } catch (Exception ex) {

        }
    }
}
