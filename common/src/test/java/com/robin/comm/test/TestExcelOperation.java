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
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Slf4j
public class TestExcelOperation {
    private Logger logger = LoggerFactory.getLogger(TestExcelOperation.class);

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
        prop.addColumnProp(new ExcelColumnProp("差距1", "sep1", Const.META_TYPE_FORMULA, "D{P+1}-D{P}"));
        prop.addColumnProp(new ExcelColumnProp("差距2", "sep2", Const.META_TYPE_FORMULA, "D{P-1}-D{P}"));
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
                if (row < 5000)
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
